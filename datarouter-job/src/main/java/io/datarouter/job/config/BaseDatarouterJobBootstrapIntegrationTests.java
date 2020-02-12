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
package io.datarouter.job.config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.scheduler.JobSchedulerTestService;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.BaseDatarouterWebBoostrapIntegrationTests;

/**
 * Extend in your webapp to help detect injection problems.
 */
public abstract class BaseDatarouterJobBootstrapIntegrationTests extends BaseDatarouterWebBoostrapIntegrationTests{

	@Inject
	private JobSchedulerTestService jobSchedulerTestService;

	@Test
	public void testJobs(){
		jobSchedulerTestService.validateCronExpressions();
	}

	@Override
	protected List<Pair<Class<?>,Boolean>> getClassesToTestSingleton(){
		List<Pair<Class<?>,Boolean>> list = new ArrayList<>(super.getClassesToTestSingleton());
		list.add(new Pair<>(BaseJob.class, false));
		list.add(new Pair<>(BaseTriggerGroup.class, true));
		return list;
	}

}
