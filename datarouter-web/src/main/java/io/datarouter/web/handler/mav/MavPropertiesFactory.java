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
package io.datarouter.web.handler.mav;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.lazy.Lazy;
import io.datarouter.web.handler.mav.nav.AppNavBar;
import io.datarouter.web.handler.mav.nav.DatarouterNavBar;
import io.datarouter.web.listener.TomcatWebAppNamesWebAppListener;

@Singleton
public class MavPropertiesFactory{

	private final MavPropertiesFactoryConfig config;
	private final Lazy<Map<String,String>> tomcatWebApps;
	private final Optional<AppNavBar> appNavBar;
	private final DatarouterNavBar datarouterNavBar;

	@Inject
	public MavPropertiesFactory(TomcatWebAppNamesWebAppListener webAppsListener, MavPropertiesFactoryConfig config,
			Optional<AppNavBar> appNavBar, DatarouterNavBar datarouterNavBar){
		this.config = config;
		this.tomcatWebApps = Lazy.of(webAppsListener::getTomcatWebApps);
		this.appNavBar = appNavBar;
		this.datarouterNavBar = datarouterNavBar;
	}

	public MavProperties get(HttpServletRequest request){
		return new MavProperties(request, config.getCssVersion(), config.getJsVersion(), config.getIsAdmin(request),
				tomcatWebApps.get(), appNavBar, config.getIsProduction(), datarouterNavBar);
	}

	public static class MavProperties{
		// attribute keys
		private static final String CONTEXT_PATH = "contextPath";
		private static final String BASE_PATH = "basePath";
		private static final String FULL_PATH = "fullPath";
		private static final String CSS_VERSION = "cssVersion";
		private static final String JS_VERSION = "jsVersion";
		private static final String IS_ADMIN = "isAdmin";
		private static final String IS_PRODUCTION = "isProduction";

		private final HttpServletRequest request;

		private final int cssVersion;
		private final int jsVersion;
		private final boolean isAdmin;
		private final boolean isProduction;
		private final Map<String,String> tomcatWebApps;
		private final Optional<AppNavBar> appNavBar;
		private final DatarouterNavBar datarouterNavBar;

		private MavProperties(HttpServletRequest request, int cssVersion, int jsVersion, boolean isAdmin,
				Map<String,String> tomcatWebApps, Optional<AppNavBar> appNavBar,
				boolean isProduction, DatarouterNavBar datarouterNavBar){
			this.request = request;
			this.cssVersion = cssVersion;
			this.jsVersion = jsVersion;
			this.isAdmin = isAdmin;
			this.tomcatWebApps = tomcatWebApps;
			this.appNavBar = appNavBar;
			this.isProduction = isProduction;
			this.datarouterNavBar = datarouterNavBar;
		}

		public Map<String,Object> getAttributes(){
			Map<String,Object> attributes = new TreeMap<>();
			attributes.put(CONTEXT_PATH, getContextPath());
			attributes.put(BASE_PATH, getBasePath());
			attributes.put(FULL_PATH, getFullPath());
			attributes.put(CSS_VERSION, getCssVersion());
			attributes.put(JS_VERSION, getJsVersion());
			attributes.put(IS_ADMIN, getIsAdmin());
			attributes.put(IS_PRODUCTION, getIsProduction());
			return attributes;
		}

		public String getContextPath(){
			return request.getContextPath();
		}

		public String getBasePath(){
			return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		}

		public String getFullPath(){
			return getBasePath() + getContextPath();
		}

		public Integer getCssVersion(){
			return cssVersion;
		}

		public Integer getJsVersion(){
			return jsVersion;
		}

		public Boolean getIsAdmin(){
			return isAdmin;
		}

		public Map<String,String> getTomcatWebApps(){
			return tomcatWebApps;
		}

		public AppNavBar getNavBar(){
			return appNavBar.orElse(null);
		}

		public DatarouterNavBar getDatarouterNavBar(){
			return datarouterNavBar;
		}

		public Boolean getIsProduction(){
			return isProduction;
		}

		public boolean isDatarouterPage(){
			return request.getRequestURI().contains("datarouter");
		}

	}

}
