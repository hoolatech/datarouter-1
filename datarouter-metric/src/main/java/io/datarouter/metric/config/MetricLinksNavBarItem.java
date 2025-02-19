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
package io.datarouter.metric.config;

import javax.inject.Inject;

import io.datarouter.web.config.service.ContextName;
import io.datarouter.web.config.service.DomainFinder;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import io.datarouter.web.user.role.DatarouterUserRole;

public class MetricLinksNavBarItem implements DynamicNavBarItem{

	@Inject
	private DatarouterMetricPaths paths;
	@Inject
	private DomainFinder domainFinder;
	@Inject
	private ContextName contextName;

	@Override
	public NavBarItem getNavBarItem(){
		String href = "https://" + domainFinder.getDomainPreferPublic() + contextName.getContextPath()
				+ paths.datarouter.metric.metricLinks.view.toSlashedString();
		return new NavBarItemBuilder(DatarouterNavBarCategory.EXTERNAL, href, "Metric Links")
				.setDispatchRule(new DispatchRule()
						.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.USER))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		return true;
	}

	@Override
	public DynamicNavBarItemType getType(){
		return DynamicNavBarItemType.DATAROUTER;
	}

}
