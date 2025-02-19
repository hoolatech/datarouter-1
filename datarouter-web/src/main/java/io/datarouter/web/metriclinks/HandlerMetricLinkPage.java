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
package io.datarouter.web.metriclinks;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;

public abstract class HandlerMetricLinkPage implements MetricLinkPage{

	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Override
	public String getName(){
		return "Handlers";
	}

	protected List<MetricLinkDto> buildMetricLinks(boolean isSystem){
		return Scanner.of(routeSetRegistry.get())
				.map(BaseRouteSet::getDispatchRules)
				.concat(Scanner::of)
				.include(rule -> {
					if(isSystem){
						return rule.isSystemDispatchRule();
					}
					return !rule.isSystemDispatchRule();
				})
				.map(DispatchRule::getHandlerClass)
				.map(Class::getSimpleName)
				.distinct()
				.concat(Scanner::of)
				.sort()
				.map(handlerName -> {
					var exactMetric = LinkDto.of("Class", "Datarouter handler class " + handlerName);
					var availbleMetric = LinkDto.of("Endpoints", "Datarouter handler method " + handlerName);
					return new MetricLinkDto(handlerName, Optional.of(exactMetric), Optional.of(availbleMetric));
				})
				.list();
	}

}
