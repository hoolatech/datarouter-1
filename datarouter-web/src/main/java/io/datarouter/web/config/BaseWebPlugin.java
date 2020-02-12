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
package io.datarouter.web.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.storage.config.BasePlugin;
import io.datarouter.util.ordered.Ordered;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.navigation.NavBarCategory;
import io.datarouter.web.navigation.NavBarItem;

/**
 * BaseWebPlugin is an extension of BasePlugin. It allows all the features from datarouter-storage and the additional
 * features from datarouter-web to be easily configured.
 *
 * BaseWebPlugin brings in auto registration and dynamic configuration of RouteSets, AppListeners, FilterParams,
 * DatarouterNavBar items and AppNavBar items.
 *
 * RouteSets, Listeners, and FilterParams can be added with or without order. To specify the order, the "after" class
 * needs to be included. Both the ordered *after sorting) and the unordered lists will be combined at runtime.
 *
 * NavBars (both datarouter and app) are sorted alphabetically for all menu items and each of the sub menu items.
 */
public abstract class BaseWebPlugin extends BasePlugin{

	/*------------------------------ route sets -----------------------------*/

	private final List<Ordered<Class<? extends BaseRouteSet>>> routeSetsOrdered = new ArrayList<>();
	private final List<Class<? extends BaseRouteSet>> routeSetsUnordered = new ArrayList<>();

	protected void addRouteSetOrdered(Class<? extends BaseRouteSet> routeSet, Class<? extends BaseRouteSet> after){
		routeSetsOrdered.add(new Ordered<>(routeSet, after));
	}

	protected void addRouteSet(Class<? extends BaseRouteSet> routeSet){
		routeSetsUnordered.add(routeSet);
	}

	public List<Ordered<Class<? extends BaseRouteSet>>> getRouteSetsOrdered(){
		return routeSetsOrdered;
	}

	public List<Class<? extends BaseRouteSet>> getRouteSetsUnordered(){
		return routeSetsUnordered;
	}

	/*------------------------------ listeners ------------------------------*/

	private final List<Ordered<Class<? extends DatarouterAppListener>>> appListenersOrdered = new ArrayList<>();
	private final List<Class<? extends DatarouterAppListener>> appListenersUnordered = new ArrayList<>();

	private final List<Ordered<Class<? extends DatarouterWebAppListener>>> orderedWebListeners = new ArrayList<>();
	private final List<Class<? extends DatarouterWebAppListener>> unorderedWebListeners = new ArrayList<>();

	protected void addAppListenerOrdered(
			Class<? extends DatarouterAppListener> listener,
			Class<? extends DatarouterAppListener> after){
		appListenersOrdered.add(new Ordered<>(listener, after));
	}

	protected void addAppListener(Class<? extends DatarouterAppListener> listener){
		appListenersUnordered.add(listener);
	}

	protected void addWebListenerOrdered(
			Class<? extends DatarouterWebAppListener> listener,
			Class<? extends DatarouterWebAppListener> after){
		orderedWebListeners.add(new Ordered<>(listener, after));
	}

	protected void addWebListener(Class<? extends DatarouterWebAppListener> listener){
		appListenersUnordered.add(listener);
	}

	public List<Ordered<Class<? extends DatarouterAppListener>>> getAppListenersOrdered(){
		return appListenersOrdered;
	}

	public List<Class<? extends DatarouterAppListener>> getAppListenersUnordered(){
		return appListenersUnordered;
	}

	public List<Ordered<Class<? extends DatarouterWebAppListener>>> getWebAppListenersOrdered(){
		return orderedWebListeners;
	}

	public List<Class<? extends DatarouterWebAppListener>> getWebAppListenersUnordered(){
		return unorderedWebListeners;
	}

	/*---------------------------- filter params ----------------------------*/

	private final List<Ordered<FilterParams>> filterParamsOrdered = new ArrayList<>();
	private final List<FilterParams> filterParamsUnordered = new ArrayList<>();

	protected void addFilterParamsOrdered(FilterParams filterParam, FilterParams after){
		filterParamsOrdered.add(new Ordered<>(filterParam, after));
	}

	protected void addFilterParams(FilterParams filterParam){
		filterParamsUnordered.add(filterParam);
	}

	public List<Ordered<FilterParams>> getdFilterParamsOrdered(){
		return filterParamsOrdered;
	}

	public List<FilterParams> getFilterParamsUnordered(){
		return filterParamsUnordered;
	}

	/*------------------------------- nav bar--------------------------------*/

	private final List<NavBarItem> datarouterNavBarItems = new ArrayList<>();
	private final List<NavBarItem> appNavBarItems = new ArrayList<>();

	protected void addDatarouterNavBarItem(NavBarCategory category, PathNode pathNode, String name){
		datarouterNavBarItems.add(new NavBarItem(category, pathNode, name));
	}

	protected void addDatarouterNavBarItem(NavBarCategory category, String path, String name){
		datarouterNavBarItems.add(new NavBarItem(category, path, name));
	}

	@Deprecated
	protected void addAppNavBarItem(NavBarItem navBarItem){
		appNavBarItems.add(navBarItem);
	}

	protected void addAppNavBarItem(NavBarCategory category, PathNode pathNode, String name){
		appNavBarItems.add(new NavBarItem(category, pathNode, name));
	}

	public List<NavBarItem> getDatarouterNavBarItems(){
		return datarouterNavBarItems;
	}

	public List<NavBarItem> getAppNavBarItems(){
		return appNavBarItems;
	}

}
