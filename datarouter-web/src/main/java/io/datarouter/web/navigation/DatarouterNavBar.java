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
package io.datarouter.web.navigation;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.navigation.DynamicNavBarItem.DynamicNavBarItemType;
import io.datarouter.web.navigation.NavBarCategory.SimpleNavBarCategory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

@Singleton
public class DatarouterNavBar extends NavBar{

	@Inject
	public DatarouterNavBar(DatarouterWebFiles webFiles, Optional<DatarouterAuthenticationConfig> config,
			DatarouterNavBarSupplier navBarSupplier, DynamicNavBarItemRegistry dynamicNavBarItemRegistry,
			DatarouterInjector injector){
		super(webFiles.jeeAssets.datarouterLogoPng.toSlashedString(), "Datarouter logo", config);
		List<NavBarItem> dynamicNavBarItems = Scanner.of(dynamicNavBarItemRegistry.items)
				.map(injector::getInstance)
				.include(item -> item.getType() == DynamicNavBarItemType.DATAROUTER)
				.include(DynamicNavBarItem::shouldDisplay)
				.map(DynamicNavBarItem::getNavBarItem)
				.list();
		Scanner.concat(navBarSupplier.get(), dynamicNavBarItems)
				.groupBy(item -> item.category.toDto())
				.entrySet()
				.stream()
				.map(this::createMenuItem)
				.sorted(Comparator.comparing((NavBarMenuItem item) -> item.getText()))
				.forEach(this::addMenuItems);
	}

	private NavBarMenuItem createMenuItem(Entry<SimpleNavBarCategory,List<NavBarItem>> entry){
		List<NavBarMenuItem> menuItems = entry.getValue().stream()
				.sorted(Comparator.comparing((NavBarItem item) -> item.name))
				.map(item -> {
					var menuItem = new NavBarMenuItem(item.path, item.name, item.openInNewTab, this);
					item.dispatchRule.ifPresent(menuItem::setDispatchRule);
					return menuItem;
				})
				.collect(Collectors.toList());
		return new NavBarMenuItem(entry.getKey().getDisplay(), menuItems);
	}

}
