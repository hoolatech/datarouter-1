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
package io.datarouter.trace.storage.trace;

import io.datarouter.trace.storage.entity.TraceEntityKey;

public class TraceKey extends BaseTraceKey<TraceEntityKey,TraceKey>{

	public TraceKey(){
		this.entityKey = new TraceEntityKey();
	}

	public TraceKey(TraceEntityKey entityKey){
		this.entityKey = entityKey;
	}

	public TraceKey(String traceId){
		this.entityKey = new TraceEntityKey(traceId);
	}

	@Override
	public TraceKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceKey(entityKey);
	}

}
