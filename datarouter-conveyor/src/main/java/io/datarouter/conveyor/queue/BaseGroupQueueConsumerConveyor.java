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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.exception.ExceptionRecorder;

public abstract class BaseGroupQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseGroupQueueConsumerConveyor.class);

	// same as the default BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS
	private static final Duration VISIBILITY_TIMEOUT = Duration.ofSeconds(30);

	private final GroupQueueConsumer<PK,D> consumer;
	private final Duration peekTimeout;

	public BaseGroupQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<PK,D> consumer,
			Supplier<Boolean> compactExceptionLogging,
			Duration peekTimeout,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, compactExceptionLogging, exceptionRecorder);
		this.consumer = consumer;
		this.peekTimeout = peekTimeout;
	}

	@Override
	public ProcessBatchResult processBatch(){
		var timer = new PhaseTimer();
		GroupQueueMessage<PK,D> message = consumer.peek(peekTimeout, VISIBILITY_TIMEOUT);
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", name);
			return new ProcessBatchResult(false);
		}
		List<D> databeans = message.getDatabeans();
		logger.info("peeked conveyor={} messageCount={}", name, databeans.size());
		timer.add("peek");
		long start = System.currentTimeMillis();
		if(!processDatabeansShouldAck(databeans)){
			return new ProcessBatchResult(true);
		}
		long durationMs = System.currentTimeMillis() - start;
		if(durationMs > VISIBILITY_TIMEOUT.toMillis()){
			logger.warn("slow conveyor conveyor={} durationMs={} databeanCount={}", name, durationMs, databeans.size());
		}
		logger.info("wrote conveyor={} messageCount={}", name, databeans.size());
		timer.add("wrote");
		ConveyorCounters.incPutMultiOpAndDatabeans(this, databeans.size());
		consumer.ack(message.getKey());
		logger.info("acked conveyor={} messageCount={}", name, databeans.size());
		timer.add("acked");
		ConveyorCounters.incAck(this);
		logger.debug("messageCount={} {}", databeans.size(), timer);
		return new ProcessBatchResult(true);
	}

	protected boolean processDatabeansShouldAck(List<D> databeans){
		processDatabeans(databeans);
		return true;
	}

	protected void processDatabeans(@SuppressWarnings("unused") List<D> databeans){
		throw new UnsupportedOperationException();
	}

}
