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
package io.datarouter.trace.conveyor.publisher;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.storage.DatarouterTracePublisherDao;

@Singleton
public class TracePublisherConveyors extends BaseConveyors{

	@Inject
	private DatarouterTracePublisherSettingRoot settings;
	@Inject
	private Gson gson;
	@Inject
	private TracePublisher tracePublisher;
	@Inject
	private TracePublisherFilterToMemoryBuffer memoryBuffer;
	@Inject
	private DatarouterTracePublisherDao tracePublisherDao;

	@Override
	public void onStartUp(){
		start(new TraceMemoryToSqsConveyorPublishing(
				"traceMemoryToSqsPublisher",
				settings.runMemoryToSqs,
				settings.bufferInSqs,
				memoryBuffer.buffer,
				tracePublisherDao::putMulti,
				gson),
				1);
		start(new TraceSqsDrainConveyorPublisher(
				"traceSqsToPublisher",
				settings.drainSqsToPublisher,
				tracePublisherDao.getGroupQueueConsumer(),
				gson,
				tracePublisher,
				settings.compactExceptionLoggingForConveyors),
				1);
	}

}
