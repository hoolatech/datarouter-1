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
package io.datarouter.tasktracker.config;

import java.util.List;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.tasktracker.service.LongRunningTaskDailyDigest;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTaskDao.LongRunningTaskDaoParams;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink.NoOpLongRunningTaskGraphLink;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink.NoOpTaskTrackerExceptionLink;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterTaskTrackerPlugin extends BaseWebPlugin{

	private final Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLinkClass;
	private final DatarouterTaskTrackerDaoModule daosModuleBuilder;
	private final Class<? extends TaskTrackerExceptionLink> exceptionLinkClass;

	private DatarouterTaskTrackerPlugin(
			Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLinkClass,
			Class<? extends TaskTrackerExceptionLink> exceptionLinkClass,
			DatarouterTaskTrackerDaoModule daosModuleBuilder){
		this.longRunningTaskGraphLinkClass = longRunningTaskGraphLinkClass;
		this.exceptionLinkClass = exceptionLinkClass;
		this.daosModuleBuilder = daosModuleBuilder;
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS,
				new DatarouterTaskTrackerPaths().datarouter.longRunningTasks, "Long running tasks");
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS,
				new DatarouterTaskTrackerPaths().datarouter.jobsHealth, "Jobs Health");
		addSettingRoot(DatarouterTaskTrackerSettingRoot.class);
		addRouteSet(DatarouterTaskTrackerRouteSet.class);
		addDatarouterGithubDocLink("datarouter-task-tracker");
		addDailyDigest(LongRunningTaskDailyDigest.class);
	}

	@Override
	public String getName(){
		return "DatarouterTaskTracker";
	}

	@Override
	public DatarouterTaskTrackerDaoModule getDaosModuleBuilder(){
		return daosModuleBuilder;
	}

	@Override
	public void configure(){
		if(longRunningTaskGraphLinkClass != null){
			bindActual(LongRunningTaskGraphLink.class, longRunningTaskGraphLinkClass);
		}
		bind(TaskTrackerExceptionLink.class).to(exceptionLinkClass);
	}

	public static class DatarouterTaskTrackerDaoModule extends DaosModuleBuilder{

		private final List<ClientId> longRunningTaskClientId;

		public DatarouterTaskTrackerDaoModule(List<ClientId> longRunningTaskClientId){
			this.longRunningTaskClientId = longRunningTaskClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(LongRunningTaskDao.class);
		}

		@Override
		public void configure(){
			bind(LongRunningTaskDaoParams.class).toInstance(new LongRunningTaskDaoParams(longRunningTaskClientId));
		}

	}

	public static class DatarouterTaskTrackerPluginBuilder{

		private final List<ClientId> defaultClientId;

		private Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLinkClass =
				NoOpLongRunningTaskGraphLink.class;
		private Class<? extends TaskTrackerExceptionLink> exceptionLinkClass =
				NoOpTaskTrackerExceptionLink.class;

		public DatarouterTaskTrackerPluginBuilder(List<ClientId> defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterTaskTrackerPluginBuilder(ClientId defaultClientId){
			this(List.of(defaultClientId));
		}

		public DatarouterTaskTrackerPluginBuilder setLongRunningTaskGraphLinkClass(
				Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLinkClass){
			this.longRunningTaskGraphLinkClass = longRunningTaskGraphLinkClass;
			return this;
		}

		public DatarouterTaskTrackerPluginBuilder setExceptionLinkClass(
				Class<? extends TaskTrackerExceptionLink> exceptionLinkClass){
			this.exceptionLinkClass = exceptionLinkClass;
			return this;
		}

		public DatarouterTaskTrackerPlugin build(){
			return new DatarouterTaskTrackerPlugin(
					longRunningTaskGraphLinkClass,
					exceptionLinkClass,
					new DatarouterTaskTrackerDaoModule(defaultClientId));
		}

	}

}
