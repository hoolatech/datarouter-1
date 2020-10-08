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
package io.datarouter.webappinstance.config;

import java.util.List;

import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher;
import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher.NoOpWebappInstancePublisher;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.webappinstance.WebappInstanceAppListener;
import io.datarouter.webappinstance.service.WebappInstanceBuildIdLink;
import io.datarouter.webappinstance.service.WebappInstanceBuildIdLink.NoOpWebappInstanceBuilIdLink;
import io.datarouter.webappinstance.service.WebappInstanceCommitIdLink;
import io.datarouter.webappinstance.service.WebappInstanceCommitIdLink.NoOpWebappInstanceCommitIdLink;
import io.datarouter.webappinstance.service.WebappInstanceDailyDigest;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao.DatarouterOneTimeLoginTokenDaoParams;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao.DatarouterWebappInstanceDaoParams;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao.DatarouterWebappInstanceLogDaoParams;

public class DatarouterWebappInstancePlugin extends BaseJobPlugin{

	private final Class<? extends WebappInstancePublisher> webappInstancePublisher;
	private final Class<? extends WebappInstanceBuildIdLink> buildIdLink;
	private final Class<? extends WebappInstanceCommitIdLink> commitIdLink;

	private DatarouterWebappInstancePlugin(
			DatarouterWebappInstanceDaoModule daosModuleBuilder,
			Class<? extends WebappInstancePublisher> webappInstancePublisher,
			Class<? extends WebappInstanceBuildIdLink> buildIdLink,
			Class<? extends WebappInstanceCommitIdLink> commitIdLink){
		this.webappInstancePublisher = webappInstancePublisher;
		this.buildIdLink = buildIdLink;
		this.commitIdLink = commitIdLink;

		addAppListener(WebappInstanceAppListener.class);
		addRouteSet(DatarouterWebappInstanceRouteSet.class);
		addSettingRoot(DatarouterWebappInstanceSettingRoot.class);
		addTriggerGroup(DatarouterWebappInstanceTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING,
				new DatarouterWebappInstancePaths().datarouter.webappInstances, "Webapp Instances");
		addDatarouterGithubDocLink("datarouter-webapp-instance");
		addDailyDigest(WebappInstanceDailyDigest.class);
	}

	@Override
	public String getName(){
		return "DatarouterWebappInstance";
	}

	@Override
	protected void configure(){
		bind(WebappInstancePublisher.class).to(webappInstancePublisher);
		bind(WebappInstanceBuildIdLink.class).to(buildIdLink);
		bind(WebappInstanceCommitIdLink.class).to(commitIdLink);
	}

	public static class DatarouterWebappInstancePluginBuilder{

		private final ClientId defaultClientId;
		private Class<? extends WebappInstancePublisher> webappInstancePublisher = NoOpWebappInstancePublisher.class;
		private Class<? extends WebappInstanceBuildIdLink> buildIdLink = NoOpWebappInstanceBuilIdLink.class;
		private Class<? extends WebappInstanceCommitIdLink> commitIdLink = NoOpWebappInstanceCommitIdLink.class;

		public DatarouterWebappInstancePluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterWebappInstancePluginBuilder withWebappInstancePublisher(
				Class<? extends WebappInstancePublisher> webappInstancePublisher){
			this.webappInstancePublisher = webappInstancePublisher;
			return this;
		}

		public DatarouterWebappInstancePluginBuilder setBuildIdLink(
				Class<? extends WebappInstanceBuildIdLink> buildIdLink){
			this.buildIdLink = buildIdLink;
			return this;
		}

		public DatarouterWebappInstancePluginBuilder setCommitIdLink(
				Class<? extends WebappInstanceCommitIdLink> commitIdLink){
			this.commitIdLink = commitIdLink;
			return this;
		}

		public DatarouterWebappInstancePlugin build(){
			return new DatarouterWebappInstancePlugin(
					new DatarouterWebappInstanceDaoModule(defaultClientId, defaultClientId, defaultClientId),
					webappInstancePublisher,
					buildIdLink,
					commitIdLink);
		}

	}

	public static class DatarouterWebappInstanceDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterOneTimeLoginTokenClientId;
		private final ClientId datarouterWebappInstanceClientId;
		private final ClientId datarouterWebappInstanceLogClientId;

		public DatarouterWebappInstanceDaoModule(
				ClientId datarouterOneTimeLoginTokenClientId,
				ClientId datarouterWebappInstanceClientId,
				ClientId datarouterWebappInstanceLogClientId){
			this.datarouterOneTimeLoginTokenClientId = datarouterOneTimeLoginTokenClientId;
			this.datarouterWebappInstanceClientId = datarouterWebappInstanceClientId;
			this.datarouterWebappInstanceLogClientId = datarouterWebappInstanceLogClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterOneTimeLoginTokenDao.class,
					DatarouterWebappInstanceDao.class,
					DatarouterWebappInstanceLogDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterWebappInstanceDaoParams.class)
					.toInstance(new DatarouterWebappInstanceDaoParams(datarouterWebappInstanceClientId));
			bind(DatarouterWebappInstanceLogDaoParams.class)
					.toInstance(new DatarouterWebappInstanceLogDaoParams(datarouterWebappInstanceLogClientId));
			bind(DatarouterOneTimeLoginTokenDaoParams.class)
					.toInstance(new DatarouterOneTimeLoginTokenDaoParams(datarouterOneTimeLoginTokenClientId));
		}

	}

}
