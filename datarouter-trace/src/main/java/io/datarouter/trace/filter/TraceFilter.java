/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.trace.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.trace.conveyor.local.Trace2ForLocalFilterToMemoryBuffer;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherFilterToMemoryBuffer;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.util.MxBeans;
import io.datarouter.util.UuidTool;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tracer.DatarouterTracer;
import io.datarouter.web.config.service.ServiceName;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RecordedHttpHeaders;
import io.datarouter.web.util.http.RequestTool;

public abstract class TraceFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

	private ServerName serverName;
	private DatarouterTraceFilterSettingRoot traceSettings;
	private Trace2ForLocalFilterToMemoryBuffer trace2BufferForLocal;
	private Trace2ForPublisherFilterToMemoryBuffer trace2BufferForPublisher;
	private TraceUrlBuilder urlBuilder;
	private CurrentSessionInfo currentSessionInfo;
	private HandlerMetrics handlerMetrics;
	private ServiceName serviceName;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		serverName = injector.getInstance(ServerName.class);
		trace2BufferForLocal = injector.getInstance(Trace2ForLocalFilterToMemoryBuffer.class);
		trace2BufferForPublisher = injector.getInstance(Trace2ForPublisherFilterToMemoryBuffer.class);
		traceSettings = injector.getInstance(DatarouterTraceFilterSettingRoot.class);
		urlBuilder = injector.getInstance(TraceUrlBuilder.class);
		currentSessionInfo = injector.getInstance(CurrentSessionInfo.class);
		handlerMetrics = injector.getInstance(HandlerMetrics.class);
		serviceName = injector.getInstance(ServiceName.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		try{
			HttpServletRequest request = (HttpServletRequest)req;
			HttpServletResponse response = (HttpServletResponse)res;

			// get or create TraceContext
			Long traceCreated = Trace2Dto.getCurrentTimeInNs();
			String traceparentStr = request.getHeader(DatarouterHttpClientIoExceptionCircuitBreaker.TRACEPARENT);
			String tracestateStr = request.getHeader(DatarouterHttpClientIoExceptionCircuitBreaker.TRACESTATE);
			W3TraceContext traceContext = new W3TraceContext(traceparentStr, tracestateStr, traceCreated);
			String initialParentId = traceContext.getTraceparent().parentId;
			traceContext.updateParentIdAndAddTracestateMember();
			RequestAttributeTool.set(request, BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE, urlBuilder
					.buildTraceForCurrentServer(traceContext.getTraceId(), traceContext.getParentId()));
			RequestAttributeTool.set(request, BaseHandler.TRACE_CONTEXT, traceContext.copy());

			// need to set the header before doFilter
			// traceflag might be incorrect because it might have been modified during request processing
			if(traceSettings.addTraceparentHeader.get()){
				response.setHeader(DatarouterHttpClientIoExceptionCircuitBreaker.TRACEPARENT,
						traceContext.getTraceparent().toString());
			}

			// bind these to all threads, even if tracing is disabled
			Tracer tracer = new DatarouterTracer(serverName.get(), null, traceContext);
			tracer.setSaveThreadCpuTime(traceSettings.saveThreadCpuTime.get());
			tracer.setSaveThreadMemoryAllocated(traceSettings.saveThreadMemoryAllocated.get());
			tracer.setSaveSpanCpuTime(traceSettings.saveSpanCpuTime.get());
			tracer.setSaveSpanMemoryAllocated(traceSettings.saveSpanMemoryAllocated.get());
			TracerThreadLocal.bindToThread(tracer);
			String requestThreadName = (request.getContextPath() + " request").trim();
			tracer.createAndStartThread(requestThreadName, Trace2Dto.getCurrentTimeInNs());

			Long threadId = Thread.currentThread().getId();
			boolean saveCpuTime = traceSettings.saveTraceCpuTime.get();
			Long cpuTimeBegin = saveCpuTime ? MxBeans.THREAD.getCurrentThreadCpuTime() : null;
			boolean saveAllocatedBytes = traceSettings.saveTraceAllocatedBytes.get();
			Long threadAllocatedBytesBegin = saveAllocatedBytes
					? MxBeans.THREAD.getThreadAllocatedBytes(threadId)
					: null;

			boolean errored = false;
			try{
				fc.doFilter(req, res);
			}catch(Exception e){
				errored = true;
				throw e;
			}finally{
				long ended = Trace2Dto.getCurrentTimeInNs();
				Long cpuTimeEnded = saveCpuTime ? MxBeans.THREAD.getCurrentThreadCpuTime() : null;
				Long threadAllocatedBytesEnded = saveAllocatedBytes ? MxBeans.THREAD.getThreadAllocatedBytes(threadId)
						: null;
				Traceparent traceparent = tracer.getTraceContext().get().getTraceparent();
				Trace2ThreadDto rootThread = null;
				if(tracer.getCurrentThreadId() != null){
					rootThread = ((DatarouterTracer)tracer).getCurrentThread();
					rootThread.setCpuTimeEndedNs(cpuTimeEnded);
					rootThread.setMemoryAllocatedBytesEnded(threadAllocatedBytesEnded);
					rootThread.setEnded(ended);
					((DatarouterTracer)tracer).setCurrentThread(null);
				}
				Trace2Dto trace2 = new Trace2Dto(
						traceparent,
						initialParentId,
						request.getContextPath(),
						request.getRequestURI().toString(),
						request.getQueryString(),
						traceCreated,
						ended,
						serviceName.get(),
						tracer.getDiscardedThreadCount(),
						tracer.getThreadQueue().size(),
						cpuTimeBegin,
						cpuTimeEnded,
						threadAllocatedBytesBegin,
						threadAllocatedBytesEnded);

				Long traceDurationMs = trace2.getDurationInMs();
				Long cpuTime = saveCpuTime ? TimeUnit.NANOSECONDS.toMillis(cpuTimeEnded - cpuTimeBegin) : null;
				Long threadAllocatedKB = saveAllocatedBytes ? (threadAllocatedBytesEnded - threadAllocatedBytesBegin)
						/ 1024 : null;
				if(traceSettings.saveTraces.get()
						&& (traceDurationMs > traceSettings.saveTracesOverMs.get()
						|| RequestTool.getBoolean(request, "trace", false)
						|| tracer.shouldSample()
						|| errored)){
					List<Trace2ThreadDto> threads = new ArrayList<>(tracer.getThreadQueue());
					List<Trace2SpanDto> spans = new ArrayList<>(tracer.getSpanQueue());
					if(rootThread != null){
						rootThread.setTotalSpanCount(spans.size());
						threads.add(rootThread); // force to save the rootThread even though the queue could be full
					}
					String userAgent = RequestTool.getUserAgent(request);
					String userToken = currentSessionInfo.getSession(request)
							.map(Session::getUserToken)
							.orElse("unknown");
					HttpRequestRecordDto httpRequestRecord = buildHttpRequestRecord(errored, request, traceCreated,
							userToken, traceparent);
					String destination = offerTrace2(new Trace2BundleDto(trace2, threads, spans),
							httpRequestRecord);
					logger.warn("Trace saved to={} traceparent={} initialParentId={} durationMs={}"
							+ " cpuTimeMs={} threadAllocatedKB={} path={} query={} userAgent=\"{}\" userToken={}",
							destination,
							traceparent,
							initialParentId,
							traceDurationMs,
							cpuTime,
							threadAllocatedKB,
							trace2.type,
							trace2.params,
							userAgent,
							userToken);
				}else if(traceDurationMs > traceSettings.logTracesOverMs.get()
						|| TracerTool.shouldLog()){
					// only log once
					logger.warn("Trace logged durationMs={} cpuTimeMs={} threadAllocatedKB={} path={}"
							+ " query={}, traceparent={}", traceDurationMs, cpuTime, threadAllocatedKB, trace2.type,
							trace2.params, traceparent);
				}
				Optional<Class<? extends BaseHandler>> handlerClassOpt = RequestAttributeTool
						.get(request, BaseHandler.HANDLER_CLASS);
				Optional<Method> handlerMethodOpt = RequestAttributeTool.get(request, BaseHandler.HANDLER_METHOD);
				if(handlerClassOpt.isPresent() && handlerMethodOpt.isPresent()){
					Class<? extends BaseHandler> handlerClass = handlerClassOpt.get();
					if(traceSettings.latencyRecordedHandlers.get().contains(handlerClass.getName())){
						handlerMetrics.saveMethodLatency(handlerClass, handlerMethodOpt.get(), traceDurationMs);
					}
				}
			}
		}finally{
			TracerThreadLocal.clearFromThread();
		}
	}

	private HttpRequestRecordDto buildHttpRequestRecord(boolean errored, HttpServletRequest request, Long receivedAt,
			String userToken, Traceparent traceparent){
		if(errored){
			// an exception in a request is recorded in the ExceptionRecorder already.
			return null;
		}
		receivedAt = TimeUnit.NANOSECONDS.toMillis(receivedAt);
		long created = TimeUnit.NANOSECONDS.toMillis(Trace2Dto.getCurrentTimeInNs());
		RecordedHttpHeaders headersWrapper = new RecordedHttpHeaders(request);
		return new HttpRequestRecordDto(UuidTool.generateV1Uuid(),
				new Date(created),
				new Date(receivedAt),
				created - receivedAt,
				null, // no exceptionRecordId
				traceparent.traceId,
				traceparent.parentId,
				request.getMethod(),
				GsonTool.GSON.toJson(request.getParameterMap()),
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				getRequestPath(request),
				request.getQueryString(),
				getBinaryBody(request),
				RequestTool.getIpAddress(request),
				currentSessionInfo.getRoles(request).toString(),
				userToken,
				headersWrapper.getAcceptCharset(),
				headersWrapper.getAcceptEncoding(),
				headersWrapper.getAcceptLanguage(),
				headersWrapper.getAccept(),
				headersWrapper.getCacheControl(),
				headersWrapper.getConnection(),
				headersWrapper.getContentEncoding(),
				headersWrapper.getContentLanguage(),
				headersWrapper.getContentLength(),
				headersWrapper.getContentType(),
				headersWrapper.getCookie(),
				headersWrapper.getDnt(),
				headersWrapper.getHost(),
				headersWrapper.getIfModifiedSince(),
				headersWrapper.getOrigin(),
				headersWrapper.getPragma(),
				headersWrapper.getReferer(),
				headersWrapper.getUserAgent(),
				headersWrapper.getXForwardedFor(),
				headersWrapper.getXRequestedWith(),
				headersWrapper.getOthers());
	}

	private static String getRequestPath(HttpServletRequest request){
		String requestUri = request.getRequestURI();
		return requestUri == null ? "" : requestUri.substring(StringTool.nullSafe(request.getContextPath()).length());
	}

	private static byte[] getBinaryBody(HttpServletRequest request){
		if(RequestAttributeTool.get(request, Dispatcher.TRANSMITS_PII).orElse(false)){
			return HttpRequestRecordDto.CONFIDENTIALITY_MSG_BYTES;
		}else{
			byte[] binaryBody = RequestTool.tryGetBodyAsByteArray(request);
			int originalLength = binaryBody.length;
			return originalLength > HttpRequestRecordDto.BINARY_BODY_MAX_SIZE ? ArrayTool.trimToSize(binaryBody,
					HttpRequestRecordDto.BINARY_BODY_MAX_SIZE) : binaryBody;
		}
	}

	private String offerTrace2(Trace2BundleDto traceBundle, HttpRequestRecordDto httpRequestRecord){
		Trace2BundleAndHttpRequestRecordDto traceAndHttpRequest = new Trace2BundleAndHttpRequestRecordDto(traceBundle,
				httpRequestRecord);
		return Stream.of(trace2BufferForLocal.offer(traceAndHttpRequest), trace2BufferForPublisher.offer(
				traceAndHttpRequest))
				.flatMap(Optional::stream)
				.collect(Collectors.joining(", "));
	}

}
