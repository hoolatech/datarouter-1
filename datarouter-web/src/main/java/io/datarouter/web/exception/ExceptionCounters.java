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
package io.datarouter.web.exception;

import io.datarouter.instrumentation.count.Counters;

public class ExceptionCounters{

	public static final String PREFIX = "Exception";

	public static void inc(String key){
		inc(key, 1L);
	}

	public static void inc(String key, long delta){
		Counters.inc(PREFIX + " " + key, delta);
	}

}
