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
package io.datarouter.util.tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.util.MxBeans;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.string.StringTool;

public class DatarouterTracer implements Tracer{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterTracer.class);

	private static final int MAX_SPANS = 200;
	private static final int MAX_THREADS = 100;

	private final String serverName;
	private final Long traceThreadParentId;
	private final Long hostThreadId;
	private final String hostThreadName;
	private final W3TraceContext w3TraceContext;

	private final BlockingQueue<Trace2ThreadDto> threadQueue = new ArrayBlockingQueue<>(MAX_THREADS);
	private final List<Trace2SpanDto> spanStack = new ArrayList<>();
	private final BlockingQueue<Trace2SpanDto> spanQueue = new ArrayBlockingQueue<>(MAX_SPANS);

	private Integer nextSpanSequence = 0;
	private int discardedSpanCount = 0;
	private int discardedThreadCount = 0;
	private Trace2ThreadDto currentThread;//should we be holding a map of current threads?  not sure yet

	private boolean saveThreadCpuTime = false;
	private boolean saveThreadMemoryAllocated = false;
	private boolean saveSpanCpuTime = false;
	private boolean saveSpanMemoryAllocated = false;

	public DatarouterTracer(String serverName, Long traceThreadParentId, W3TraceContext w3TraceContext){
		this.serverName = serverName;
		this.traceThreadParentId = traceThreadParentId;
		this.hostThreadId = Thread.currentThread().getId();
		this.hostThreadName = Thread.currentThread().getName();
		this.w3TraceContext = w3TraceContext;
	}

	/*---------------------------- Tracer------------------------------------*/

	@Override
	public Tracer createChildTracer(){
		Tracer childTracer = new DatarouterTracer(serverName, getCurrentThreadId(), w3TraceContext);
		childTracer.setSaveThreadCpuTime(saveThreadCpuTime);
		childTracer.setSaveThreadMemoryAllocated(saveThreadMemoryAllocated);
		childTracer.setSaveSpanCpuTime(saveSpanCpuTime);
		childTracer.setSaveSpanMemoryAllocated(saveSpanMemoryAllocated);
		return childTracer;
	}

	/*---------------------------- TraceThread ------------------------------*/

	@Override
	public Long getCurrentThreadId(){
		if(getCurrentThread() == null){
			return null;
		}
		return getCurrentThread().getThreadId();
	}

	@Override
	public void createThread(String name, long queueTimeNs){
		if(w3TraceContext == null){
			return;
		}
		Long parentId = getTraceThreadParentId();
		Long threadId = parentId == null ? 0L : RandomTool.nextPositiveLong();

		Trace2ThreadDto thread = new Trace2ThreadDto(
				w3TraceContext.getTraceparent(),
				threadId,
				parentId,
				name,
				getServerName(),
				hostThreadName,
				queueTimeNs);
		setCurrentThread(thread);
	}

	@SuppressWarnings("restriction")
	@Override
	public void startThread(){
		if(getCurrentThread() == null){
			return;
		}
		Trace2ThreadDto thread = getCurrentThread();
		if(saveThreadCpuTime){
			thread.setCpuTimeCreatedNs(MxBeans.THREAD.getCurrentThreadCpuTime());
		}
		if(saveThreadMemoryAllocated){
			thread.setMemoryAllocatedBytesBegin(MxBeans.THREAD.getThreadAllocatedBytes(hostThreadId));
		}
		thread.markStart();
	}

	@Override
	public void appendToThreadInfo(String text){
		if(getCurrentThread() == null){
			return;
		}
		Trace2ThreadDto thread = getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getInfo());
		thread.setInfo(StringTool.nullSafe(thread.getInfo()) + (addSpace ? " " : "") + text);
	}

	@SuppressWarnings("restriction")
	@Override
	public void finishThread(){
		if(getCurrentThread() == null){
			return;
		}
		Trace2ThreadDto thread = getCurrentThread();
		if(saveThreadCpuTime){
			thread.setCpuTimeEndedNs(MxBeans.THREAD.getCurrentThreadCpuTime());
		}
		if(saveThreadMemoryAllocated){
			thread.setMemoryAllocatedBytesEnded(MxBeans.THREAD.getThreadAllocatedBytes(hostThreadId));
		}
		thread.setEnded(Trace2Dto.getCurrentTimeInNs());
		thread.setTotalSpanCount(getSpanQueue().size());
		setCurrentThread(null);
		addThread(thread);
	}

	@Override
	public void addThread(Trace2ThreadDto thread){
		if(!getThreadQueue().offer(thread)){
			++discardedThreadCount;
			logger.debug("cannot add thread, max capacity reached traceId={}, discarded thread count={}", w3TraceContext
					.getTraceparent(), discardedThreadCount);
		}
	}


	/*---------------------------- TraceSpan --------------------------------*/

	@SuppressWarnings("restriction")
	@Override
	public void startSpan(String name, TraceSpanGroupType groupType){
		if(currentThread == null){
			return;
		}
		Integer parentSequence = null;
		List<Trace2SpanDto> spanStack = getSpanStack();
		if(spanStack != null && !spanStack.isEmpty()){
			Trace2SpanDto parent = getSpanStack().get(getSpanStack().size() - 1);
			parentSequence = parent.getSequence();
		}
		Trace2SpanDto span = new Trace2SpanDto(
				currentThread.getTraceparent(),
				currentThread.getThreadId(),
				nextSpanSequence,
				parentSequence,
				name,
				groupType,
				Trace2Dto.getCurrentTimeInNs());
		if(saveSpanCpuTime){
			span.setCpuTimeCreated(MxBeans.THREAD.getCurrentThreadCpuTime());
		}
		if(saveSpanMemoryAllocated){
			span.setMemoryAllocatedBegin(MxBeans.THREAD.getThreadAllocatedBytes(hostThreadId));
		}
		getSpanStack().add(span);
		++nextSpanSequence;
	}

	@Override
	public void appendToSpanInfo(String text){
		if(getCurrentSpan() == null){
			return;
		}
		Trace2SpanDto span = getCurrentSpan();
		span.setInfo(StringTool.nullSafe(span.getInfo()) + '[' + text + ']');
	}

	@SuppressWarnings("restriction")
	@Override
	public void finishSpan(){
		if(getCurrentSpan() == null){
			return;
		}
		Trace2SpanDto span = popSpanFromStack();
		if(saveSpanCpuTime){
			span.setCpuTimeEndedNs(MxBeans.THREAD.getCurrentThreadCpuTime());
		}
		if(saveSpanMemoryAllocated){
			span.setMemoryAllocatedBytesEnded(MxBeans.THREAD.getThreadAllocatedBytes(hostThreadId));
		}
		span.markFinish();
		addSpan(span);
	}

	@Override
	public void addSpan(Trace2SpanDto span){
		if(currentThread == null){
			return;
		}
		if(!getSpanQueue().offer(span)){
			currentThread.setDiscardedSpanCount(++discardedSpanCount);
			logger.debug("cannot add span, max capacity traceId={}, discarded span count={}", w3TraceContext
					.getTraceparent(), discardedSpanCount);
		}
	}

	@Override
	public Trace2SpanDto getCurrentSpan(){
		if(spanStack == null || spanStack.isEmpty()){
			return null;
		}
		return spanStack.get(spanStack.size() - 1);
	}

	private Trace2SpanDto popSpanFromStack(){
		if(spanStack == null || spanStack.isEmpty()){
			return null;
		}
		return spanStack.remove(spanStack.size() - 1);
	}

	/*---------------------------- object -----------------------------------*/

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + currentThread.getName() + "]";
	}

	/*---------------------------- get/set ----------------------------------*/

	public Trace2ThreadDto getCurrentThread(){
		return currentThread;
	}

	@Override
	public void incrementDiscardedThreadCount(int discardedThreadCount){
		this.discardedThreadCount += discardedThreadCount;
	}

	@Override
	public Integer getDiscardedThreadCount(){
		return discardedThreadCount;
	}

	public void setCurrentThread(Trace2ThreadDto currentThread){
		this.currentThread = currentThread;
	}

	@Override
	public BlockingQueue<Trace2ThreadDto> getThreadQueue(){
		return threadQueue;
	}

	@Override
	public BlockingQueue<Trace2SpanDto> getSpanQueue(){
		return spanQueue;
	}

	@Override
	public void incrementDiscardedSpanCount(int discardedSpanCount){
		this.discardedSpanCount += discardedSpanCount;
	}

	@Override
	public Integer getDiscardedSpanCount(){
		return discardedSpanCount;
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	public Long getTraceThreadParentId(){
		return traceThreadParentId;
	}

	public List<Trace2SpanDto> getSpanStack(){
		return spanStack;
	}

	@Override
	public void setForceSample(){
		w3TraceContext.getTraceparent().enableSample();
	}

	@Override
	public boolean shouldSample(){
		return w3TraceContext.getTraceparent().shouldSample();
	}

	@Override
	public void setForceLog(){
		w3TraceContext.getTraceparent().enableLog();
	}

	@Override
	public boolean shouldLog(){
		return w3TraceContext.getTraceparent().shouldLog();
	}

	@Override
	public Optional<W3TraceContext> getTraceContext(){
		return Optional.of(w3TraceContext);
	}

	@Override
	public void setSaveThreadCpuTime(boolean saveThreadCpuTime){
		this.saveThreadCpuTime = saveThreadCpuTime;
	}

	@Override
	public void setSaveThreadMemoryAllocated(boolean saveThreadMemoryAllocated){
		this.saveThreadMemoryAllocated = saveThreadMemoryAllocated;
	}

	@Override
	public void setSaveSpanCpuTime(boolean saveSpanCpuTime){
		this.saveSpanCpuTime = saveSpanCpuTime;
	}

	@Override
	public void setSaveSpanMemoryAllocated(boolean saveSpanMemoryAllocated){
		this.saveSpanMemoryAllocated = saveSpanMemoryAllocated;
	}

}
