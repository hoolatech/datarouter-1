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
package io.datarouter.job.metriclink;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.metriclinks.MetricLinkDto;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;
import io.datarouter.web.metriclinks.MetricLinkPage;

public abstract class JobMetricLinkPage implements MetricLinkPage{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;

	@Override
	public String getName(){
		return "Jobs";
	}

	protected List<MetricLinkDto> buildMetricLinks(boolean isSystem){
		List<? extends BaseTriggerGroup> triggerGroups = injector.getInstances(triggerGroupClasses.get());
		return Scanner.of(triggerGroups)
				.include(triggerGroup -> {
					if(isSystem){
						return triggerGroup.isSystemTriggerGroup;
					}
					return !triggerGroup.isSystemTriggerGroup;
				})
				.map(BaseTriggerGroup::getJobPackages)
				.concat(Scanner::of)
				.map(JobPackage::toString)
				.map(jobName -> {
					var availbleMetric = LinkDto.of("Datarouter job " + jobName);
					return new MetricLinkDto(jobName, Optional.empty(), Optional.of(availbleMetric));
				})
				.list();
	}

}
