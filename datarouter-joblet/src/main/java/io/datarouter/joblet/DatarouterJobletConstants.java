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
package io.datarouter.joblet;

import java.time.Duration;

public class DatarouterJobletConstants{

	public static final String QUEUE_PREFIX = "Jblt2-";

	//  "my-namespace-" + "Joblet-" + shortQueueName + "-0100"
	public static final int
			MAX_LENGTH_NAMESPACE = 30,
			MAX_LENGTH_SHORT_QUEUE_NAME = 38;

	public static final long RUNNING_JOBLET_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();

}
