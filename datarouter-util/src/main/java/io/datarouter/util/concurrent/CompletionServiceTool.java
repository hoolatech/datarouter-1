package io.datarouter.util.concurrent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.datarouter.util.exception.InterruptedRuntimeException;

public class CompletionServiceTool{

	public static <T> Iterator<T> createResultIterator(ExecutorService executor,
			Iterator<? extends Callable<T>> callableIterator, int numThreads){
		return new ResultIterator<>(executor, callableIterator, numThreads);
	}

	private static class ResultIterator<T> implements Iterator<T>{
		private final CompletionService<T> completionService;
		private final Iterator<? extends Callable<T>> callableIterator;
		private final Set<Future<T>> runningFutures;

		public ResultIterator(ExecutorService executor, Iterator<? extends Callable<T>> callableIterator,
				int numThreads){
			this.completionService = new ExecutorCompletionService<>(executor);
			this.callableIterator = callableIterator;
			this.runningFutures = new HashSet<>();

			//submit the first x callables
			while(runningFutures.size() < numThreads && callableIterator.hasNext()){
				runningFutures.add(completionService.submit(callableIterator.next()));
			}
		}

		@Override
		public boolean hasNext(){
			return runningFutures.size() > 0;
		}

		@Override
		public T next(){
			Future<T> future = null;
			T result;
			try{
				future = completionService.take();
				result = future.get();
			}catch(InterruptedException e){
				runningFutures.forEach(runningFuture -> runningFuture.cancel(true));
				runningFutures.clear();
				Thread.currentThread().interrupt();
				throw new InterruptedRuntimeException(e);
			}catch(ExecutionException e){
				throw new RuntimeException(e);
			}finally{
				if(future != null){
					runningFutures.remove(future);
				}
				if(!Thread.currentThread().isInterrupted() && callableIterator.hasNext()){
					runningFutures.add(completionService.submit(callableIterator.next()));
				}
			}
			return result;
		}

	}

}
