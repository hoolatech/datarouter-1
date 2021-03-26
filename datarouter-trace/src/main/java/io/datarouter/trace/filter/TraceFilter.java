/**
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
import java.lang.management.ManagementFactory;
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

import com.sun.management.ThreadMXBean;

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.trace.conveyor.local.FilterToMemoryBufferForLocal;
import io.datarouter.trace.conveyor.local.Trace2ForLocalFilterToMemoryBuffer;
import io.datarouter.trace.conveyor.publisher.FilterToMemoryBufferForPublisher;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherFilterToMemoryBuffer;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.util.UlidTool;
import io.datarouter.util.UuidTool;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tracer.DatarouterTracer;
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

	private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);

	private DatarouterProperties datarouterProperties;
	private DatarouterTraceFilterSettingRoot traceSettings;
	private FilterToMemoryBufferForLocal traceBufferForLocal;
	private Trace2ForLocalFilterToMemoryBuffer trace2BufferForLocal;
	private FilterToMemoryBufferForPublisher traceBufferForPublisher;
	private Trace2ForPublisherFilterToMemoryBuffer trace2BufferForPublisher;
	private TraceUrlBuilder urlBuilder;
	private CurrentSessionInfo currentSessionInfo;
	private HandlerMetrics handlerMetrics;
	private DatarouterService datarouterService;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		datarouterProperties = injector.getInstance(DatarouterProperties.class);
		traceBufferForLocal = injector.getInstance(FilterToMemoryBufferForLocal.class);
		trace2BufferForLocal = injector.getInstance(Trace2ForLocalFilterToMemoryBuffer.class);
		traceBufferForPublisher = injector.getInstance(FilterToMemoryBufferForPublisher.class);
		trace2BufferForPublisher = injector.getInstance(Trace2ForPublisherFilterToMemoryBuffer.class);
		traceSettings = injector.getInstance(DatarouterTraceFilterSettingRoot.class);
		urlBuilder = injector.getInstance(TraceUrlBuilder.class);
		currentSessionInfo = injector.getInstance(CurrentSessionInfo.class);
		handlerMetrics = injector.getInstance(HandlerMetrics.class);
		datarouterService = injector.getInstance(DatarouterService.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		try{
			HttpServletRequest request = (HttpServletRequest)req;
			HttpServletResponse response = (HttpServletResponse)res;

			String traceId = UlidTool.nextUlid();
			RequestAttributeTool.set(request, BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE, urlBuilder
					.buildTraceForCurrentServer(traceId));
			if(traceSettings.addTraceIdHeader.get()){
				response.setHeader(DatarouterHttpClientIoExceptionCircuitBreaker.X_TRACE_ID, traceId);
			}
			Long created = Trace2Dto.getCurrentTimeInNs();
			TraceDto trace = new TraceDto(traceId, created);
			trace.setContext(request.getContextPath());
			trace.setType(request.getRequestURI().toString());
			trace.setParams(request.getQueryString());

			// get or create TraceContext
			String traceparent = request.getHeader(DatarouterHttpClientIoExceptionCircuitBreaker.TRACEPARENT);
			String tracestate = request.getHeader(DatarouterHttpClientIoExceptionCircuitBreaker.TRACESTATE);
			W3TraceContext traceContext = new W3TraceContext(traceparent, tracestate, created);
			String initialParentId = traceContext.getTraceparent().parentId;
			traceContext.updateParentIdAndAddTracestateMember();
			RequestAttributeTool.set(request, BaseHandler.TRACE_CONTEXT, traceContext.copy());

			// bind these to all threads, even if tracing is disabled
			String serverName = datarouterProperties.getServerName();
			Tracer tracer = new DatarouterTracer(serverName, traceId, null, traceContext);
			TracerThreadLocal.bindToThread(tracer);

			String requestThreadName = (request.getContextPath() + " request").trim();
			tracer.createAndStartThread(requestThreadName, Trace2Dto.getCurrentTimeInNs());

			Long threadId = Thread.currentThread().getId();
			boolean logCpuTime = traceSettings.logCpuTime.get();
			Long cpuTimeBegin = logCpuTime ? THREAD_MX_BEAN.getThreadCpuTime(threadId) : null;
			boolean logAllocatedBytes = traceSettings.logAllocatedBytes.get();
			Long threadAllocatedBytesBegin = logAllocatedBytes
					? THREAD_MX_BEAN.getThreadAllocatedBytes(threadId)
					: null;

			boolean errored = false;
			try{
				fc.doFilter(req, res);
			}catch(Exception e){
				errored = true;
				throw e;
			}finally{
				Long cpuTime = logCpuTime
						? THREAD_MX_BEAN.getThreadCpuTime(threadId) - cpuTimeBegin
						: null;
				if(cpuTime != null){
					cpuTime = TimeUnit.NANOSECONDS.toMillis(cpuTime);
				}
				Long threadAllocatedKB = logAllocatedBytes
						? (THREAD_MX_BEAN.getThreadAllocatedBytes(threadId) - threadAllocatedBytesBegin) / 1024
						: null;

				tracer.finishThread();
				trace.markFinished();

				boolean saveTraces = traceSettings.saveTraces.get();
				int saveCutoff = traceSettings.saveTracesOverMs.get();
				boolean requestForceSave = RequestTool.getBoolean(request, "trace", false);
				boolean tracerForceSave = tracer.getForceSave();

				Trace2Dto trace2Dto = createTrace2Dto(traceContext, initialParentId, request, created, tracer);
				Long traceDurationMs = trace.getDurationMs();
				if(saveTraces && traceDurationMs > saveCutoff || requestForceSave || tracerForceSave || errored){
					List<TraceThreadDto> threads = new ArrayList<>(tracer.getThreadQueue());
					List<TraceSpanDto> spans = new ArrayList<>(tracer.getSpanQueue());
					trace.setDiscardedThreadCount(tracer.getDiscardedThreadCount());
					String userAgent = RequestTool.getUserAgent(request);
					String userToken = currentSessionInfo.getSession(request)
							.map(Session::getUserToken)
							.orElse("unknown");
					TraceEntityDto entityDto = new TraceEntityDto(trace, threads, spans);
					String destination = offer(entityDto);
					// temporary use Trace to create Trace2 since they have most of the data the same except for
					// traceId and TracePerent
					List<Trace2ThreadDto> trace2Threads = convertToTrace2Thread(threads, traceContext.getTraceparent(),
							spans.size());
					List<Trace2SpanDto> trace2Spans = convertToTrace2Span(spans, traceContext.getTraceparent());
					HttpRequestRecordDto httpRequestRecord = buildHttpRequestRecord(errored, request, created,
							userToken, traceContext.getTraceparent());
					String destination2 = offerTrace2(new Trace2BundleDto(trace2Dto, trace2Threads, trace2Spans),
							httpRequestRecord);
					logger.warn("Trace saved to={} traceId={} traceparent={} initialParentId={} durationMs={}"
							+ " cpuTimeMs={} threadAllocatedKB={} path={} query={} userAgent=\"{}\" userToken={}",
							String.join(",", destination, destination2),
							trace.getTraceId(),
							traceContext.getTraceparent(),
							initialParentId,
							traceDurationMs,
							cpuTime,
							threadAllocatedKB,
							trace.getType(),
							trace.getParams(),
							userAgent,
							userToken);
				}else if(traceDurationMs > traceSettings.logTracesOverMs.get()){
					// only log once
					logger.warn("Trace logged durationMs={} cpuTimeMs={} threadAllocatedKB={} path={}"
							+ " query={}, traceContext={}", traceDurationMs, cpuTime, threadAllocatedKB, trace
									.getType(), trace.getParams(), traceContext);
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

	private String offer(TraceEntityDto dto){
		return Stream.of(traceBufferForLocal.offer(dto), traceBufferForPublisher.offer(dto))
				.flatMap(Optional::stream)
				.collect(Collectors.joining(", "));
	}

	private String offerTrace2(Trace2BundleDto traceBundle, HttpRequestRecordDto httpRequestRecord){
		Trace2BundleAndHttpRequestRecordDto traceAndHttpRequest = new Trace2BundleAndHttpRequestRecordDto(traceBundle,
				httpRequestRecord);
		return Stream.of(trace2BufferForLocal.offer(traceAndHttpRequest), trace2BufferForPublisher.offer(
				traceAndHttpRequest))
				.flatMap(Optional::stream)
				.collect(Collectors.joining(", "));
	}

	private Trace2Dto createTrace2Dto(W3TraceContext traceContext, String initialParentId, HttpServletRequest request,
			Long created, Tracer tracer){
		return new Trace2Dto(
				traceContext.getTraceparent(),
				initialParentId,
				request.getContextPath(),
				request.getRequestURI().toString(),
				request.getQueryString(),
				created,
				datarouterService.getServiceName(),
				tracer.getDiscardedThreadCount(),
				tracer.getThreadQueue().size());
	}

	/*
	 * TODO remove this when we stop sending Trace (v1) to pontoon.
	 */
	private List<Trace2ThreadDto> convertToTrace2Thread(List<TraceThreadDto> threads, Traceparent traceparent,
			int numSpans){
		return Scanner.of(threads)
				.map(thread -> new Trace2ThreadDto(
						traceparent,
						thread.getThreadId(),
						thread.getParentId(),
						thread.getName(),
						thread.getInfo(),
						thread.getServerId(),
						thread.getCreated(),
						thread.getQueuedEnded(),
						thread.getEnded(),
						thread.getDiscardedSpanCount(),
						thread.getHostThreadName(),
						numSpans))
				.list();
	}

	/*
	 * TODO remove this when we stop sending Trace (v1) to pontoon.
	 */
	private List<Trace2SpanDto> convertToTrace2Span(List<TraceSpanDto> spans, Traceparent traceparent){
		return Scanner.of(spans)
				.map(span -> new Trace2SpanDto(
						traceparent,
						span.getThreadId(),
						span.getSequence(),
						span.getParentSequence(),
						span.getName(),
						span.getInfo(),
						span.getCreated(),
						span.getEnded()))
				.list();
	}

}
