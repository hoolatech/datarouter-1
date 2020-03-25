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
package io.datarouter.metric.counter.collection.archive;

import io.datarouter.util.ComparableTool;
import io.datarouter.util.lang.ClassTool;

public abstract class BaseCountArchive implements CountArchive{

	protected final String serviceName;
	protected final String source;
	protected final Long periodMs;

	public BaseCountArchive(String serviceName, String source, Long periodMs){
		this.serviceName = serviceName;
		this.source = source;
		this.periodMs = periodMs;
	}

	@Override
	public int compareTo(CountArchive that){
		if(ClassTool.differentClass(this, that)){
			return ComparableTool.nullFirstCompareTo(this.getClass().getName(), that.getClass().getName());
		}
		return (int)(this.getPeriodMs() - that.getPeriodMs());
	}

	@Override
	public long getPeriodMs(){
		return this.periodMs;
	}

}
