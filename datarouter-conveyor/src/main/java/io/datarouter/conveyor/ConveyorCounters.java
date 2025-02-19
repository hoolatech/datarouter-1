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
package io.datarouter.conveyor;

import io.datarouter.instrumentation.count.Counters;

public class ConveyorCounters{

	private static final String PREFIX = "Conveyor";

	public static void inc(Conveyor conveyor, String action, long by){
		Counters.inc(PREFIX + " " + action, by);
		Counters.inc(PREFIX + " " + conveyor.getName() + " " + action, by);
	}

	public static void inc(Buffer buffer, String action, long by){
		Counters.inc(PREFIX + " buffer " + action, by);
		Counters.inc(PREFIX + " buffer " + buffer.getName() + " " + action, by);
	}

	public static void incPutMultiOpAndDatabeans(Conveyor conveyor, long numDatabeans){
		inc(conveyor, "putMulti ops", 1);
		inc(conveyor, "putMulti databeans", numDatabeans);
	}

	public static void incConsumedOpAndDatabeans(Conveyor conveyor, long numDatabeans){
		inc(conveyor, "consumed ops", 1);
		inc(conveyor, "consumed databeans", numDatabeans);
	}

	public static void incConsumedOpAndDatabeansWithPriority(Conveyor conveyor, long numDatabeans, String priority){
		incConsumedOpAndDatabeans(conveyor, numDatabeans);
		inc(conveyor, priority + " consumed ops", 1);
		inc(conveyor, priority + " consumed databeans", numDatabeans);
	}

	public static void incAck(Conveyor conveyor){
		inc(conveyor, "ack", 1);
	}

	public static void incAck(Conveyor conveyor, long numDatabeans){
		inc(conveyor, "ack", numDatabeans);
	}

	public static void incAckWithPriority(Conveyor conveyor, String priority){
		incAck(conveyor);
		inc(conveyor, priority + " ack", 1);
	}

	public static void incProcessBatch(Conveyor conveyor){
		inc(conveyor, "processBatch", 1);
	}

	public static void incInterrupted(Conveyor conveyor){
		inc(conveyor, "interrupted", 1);
	}

	public static void incException(Conveyor conveyor){
		inc(conveyor, "exception", 1);
	}

	public static void incFinishDrain(Conveyor conveyor){
		inc(conveyor, "finishDrain", 1);
	}

	public static void incFlushBuffer(Conveyor conveyor, long numDatabeans){
		inc(conveyor, "flushBuffer", numDatabeans);
	}

}
