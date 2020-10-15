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

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.trace.conveyor.BaseTraceMemoryToSqsConveyor;
import io.datarouter.web.exception.ExceptionRecorder;

public class TraceMemoryToSqsConveyorPublishing extends BaseTraceMemoryToSqsConveyor{

	private final Consumer<Collection<ConveyorMessage>> putMultiConsumer;
	private final Setting<Boolean> shouldBufferInSqs;

	public TraceMemoryToSqsConveyorPublishing(
			String name,
			Setting<Boolean> shouldRunSetting,
			Setting<Boolean> shouldBufferInSqs,
			MemoryBuffer<TraceEntityDto> buffer,
			Consumer<Collection<ConveyorMessage>> putMultiConsumer,
			Gson gson,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRunSetting, buffer, gson, exceptionRecorder);
		this.shouldBufferInSqs = shouldBufferInSqs;
		this.putMultiConsumer = putMultiConsumer;
	}

	@Override
	public void processTraceEntityDtos(List<TraceEntityDto> dtos){
		if(shouldBufferInSqs.get()){
			Scanner.of(dtos).map(this::toMessage).flush(putMultiConsumer::accept);
		}
	}

}
