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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

@Singleton
public class DefaultAppNavBar extends AppNavBar{

	@Inject
	public DefaultAppNavBar(
			Optional<DatarouterAuthenticationConfig> config,
			AppPluginNavBarSupplier appNavBarPluginSupplier,
			AppNavBarRegistrySupplier registrySupplier,
			DynamicNavBarItemRegistry dynamicNavBarItemRegistry,
			DatarouterInjector injector){
		super(config, appNavBarPluginSupplier, registrySupplier, dynamicNavBarItemRegistry, injector);
	}

}
