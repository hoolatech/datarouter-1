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
package io.datarouter.joblet.job;

import java.time.Duration;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class JobletInstanceCounterJob extends BaseJob{

	public static final Duration HEARTBEAT_WITHIN = Duration.ofMinutes(3);

	@Inject
	private ServerTypes serverTypes;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;

	@Override
	public void run(TaskTracker tracker){
		List<WebappInstance> jobletInstances = webappInstanceDao.getWebappInstancesOfServerType(
				serverTypes.getJobletServerType(),
				HEARTBEAT_WITHIN);
		datarouterJobletCounters.saveNumServers(WebappInstance.getUniqueServerNames(jobletInstances).size());
	}

}
