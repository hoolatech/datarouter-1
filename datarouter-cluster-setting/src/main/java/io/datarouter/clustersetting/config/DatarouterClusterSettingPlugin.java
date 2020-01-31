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
package io.datarouter.clustersetting.config;

import java.util.List;

import io.datarouter.clustersetting.listener.SettingNodeValidationAppListener;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao.DatarouterClusterSettingDaoParams;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao.DatarouterClusterSettingLogDaoParams;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.NavBarItem;

public class DatarouterClusterSettingPlugin extends BaseJobPlugin{

	private DatarouterClusterSettingPlugin(DatarouterClusterSettingDaoModule daosModuleBuilder){
		addSettingRoot(DatarouterClusterSettingRoot.class);
		addUnorderedRouteSet(DatarouterClusterSettingRouteSet.class);
		addUnorderedAppListener(SettingNodeValidationAppListener.class);
		addTriggerGroup(DatarouterClusterSettingTriggerGroup.class);
		String browseSettings = new DatarouterClusterSettingPaths().datarouter.settings.toSlashedString()
				+ "?submitAction=browseSettings";
		String settingLogs = new DatarouterClusterSettingPaths().datarouter.settings.toSlashedString()
				+ "?submitAction=logsForAll";
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.SETTINGS, browseSettings, "Browse Settings"));
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.SETTINGS, settingLogs, "Setting logs"));
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.SETTINGS,
				new DatarouterClusterSettingPaths().datarouter.settings, "Custom Settings"));
		setDaosModuleBuilder(daosModuleBuilder);
	}

	@Override
	public String getName(){
		return "DatarouterClusterSetting";
	}

	public static class DatarouterClusterSettingPluginBuilder{

		private final ClientId defaultClientId;
		private DatarouterClusterSettingDaoModule daoModule;

		public DatarouterClusterSettingPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterClusterSettingPluginBuilder setDaoModule(DatarouterClusterSettingDaoModule module){
			this.daoModule = module;
			return this;
		}

		public DatarouterClusterSettingPlugin build(){
			return new DatarouterClusterSettingPlugin(daoModule == null
					? new DatarouterClusterSettingDaoModule(defaultClientId, defaultClientId)
					: daoModule);
		}

	}

	public static class DatarouterClusterSettingDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterClusterSettingClientId;
		private final ClientId datarouterClusterSettingLogClientId;

		public DatarouterClusterSettingDaoModule(
				ClientId datarouterClusterSettingClientId,
				ClientId datarouterClusterSettingLogClientId){
			this.datarouterClusterSettingClientId = datarouterClusterSettingClientId;
			this.datarouterClusterSettingLogClientId = datarouterClusterSettingLogClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterClusterSettingDao.class,
					DatarouterClusterSettingLogDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterClusterSettingDaoParams.class)
					.toInstance(new DatarouterClusterSettingDaoParams(datarouterClusterSettingClientId));
			bind(DatarouterClusterSettingLogDaoParams.class)
					.toInstance(new DatarouterClusterSettingLogDaoParams(datarouterClusterSettingLogClientId));
		}

	}

}
