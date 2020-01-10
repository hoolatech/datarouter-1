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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.storage.setting.AdditionalSettingRoots;
import io.datarouter.storage.setting.AdditionalSettingRootsSupplier;

public class DatarouterStoragePlugin extends BasePlugin{

	private final ServerType serverType;
	private final Class<? extends ServerTypeDetector> serverTypeDetectorClass;
	private final Class<? extends DatarouterProperties> datarouterPropertiesClass;
	private final Class<?> settingOverridesClass;
	private final AdditionalSettingRoots additionalSettingRoots;
	private final Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
	private final List<Class<? extends Dao>> daoClasses;

	// only used to get simple data from plugin
	private DatarouterStoragePlugin(){
		this(null, null, null, null, null, null, null);
	}

	private DatarouterStoragePlugin(
			ServerType serverType,
			Class<? extends ServerTypeDetector> serverTypeDetectorClass,
			Class<? extends DatarouterProperties> datarouterPropertiesClass,
			Class<?> settingOverridesClass,
			AdditionalSettingRoots additionalSettingRoots,
			Class<? extends ClientOptionsFactory> clientOptionsFactoryClass,
			List<Class<? extends Dao>> daoClasses){
		addSettingRoot(DatarouterStorageSettingRoot.class);
		this.serverType = serverType;
		this.serverTypeDetectorClass = serverTypeDetectorClass;
		this.datarouterPropertiesClass = datarouterPropertiesClass;
		this.settingOverridesClass = settingOverridesClass;
		this.additionalSettingRoots = additionalSettingRoots;
		this.clientOptionsFactoryClass = clientOptionsFactoryClass;
		this.daoClasses = daoClasses;
	}

	@Override
	public void configure(){
		bind(ServerType.class).toInstance(serverType);
		bindActual(ServerTypeDetector.class, serverTypeDetectorClass);
		bind(DatarouterProperties.class).to(datarouterPropertiesClass);
		if(settingOverridesClass != null){
			bind(settingOverridesClass).asEagerSingleton(); // allow overriders in tests;
		}
		if(additionalSettingRoots != null){
			bindActualInstance(AdditionalSettingRootsSupplier.class, additionalSettingRoots);
		}
		if(clientOptionsFactoryClass != null){
			bindActual(ClientOptionsFactory.class, clientOptionsFactoryClass);
		}
		bind(DaoClasses.class).toInstance(new DaoClasses(daoClasses));
	}

	public static class DatarouterStoragePluginBuilder{

		private final ServerType serverType;
		private final Class<? extends DatarouterProperties> datarouterPropertiesClass;

		private Class<? extends ServerTypeDetector> serverTypeDetectorClass = NoOpServerTypeDetector.class;
		private Class<?> settingOverridesClass;
		private AdditionalSettingRoots additionalSettingRoots;
		private Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
		private List<Class<? extends Dao>> daoClasses = new ArrayList<>();

		public DatarouterStoragePluginBuilder(ServerType serverType,
				Class<? extends DatarouterProperties> datarouterPropertiesClass){
			this.serverType = serverType;
			this.datarouterPropertiesClass = datarouterPropertiesClass;
		}

		public DatarouterStoragePluginBuilder setServerTypeDetector(
				Class<? extends ServerTypeDetector> serveTypeDetectorClass){
			this.serverTypeDetectorClass = serveTypeDetectorClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setSettingOverridesClass(Class<?> settingOverridesClass){
			this.settingOverridesClass = settingOverridesClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setAdditionalSettingRootsClass(
				AdditionalSettingRoots additionalSettingRoots){
			this.additionalSettingRoots = additionalSettingRoots;
			return this;
		}

		public DatarouterStoragePluginBuilder setClientOptionsFactoryClass(
				Class<? extends ClientOptionsFactory> clientOptionsFactoryClass){
			this.clientOptionsFactoryClass = clientOptionsFactoryClass;
			return this;
		}

		public DatarouterStoragePluginBuilder addDao(Class<? extends Dao> dao){
			this.daoClasses.add(dao);
			return this;
		}

		public DatarouterStoragePluginBuilder addDaosClasses(List<Class<? extends Dao>> daos){
			this.daoClasses.addAll(daos);
			return this;
		}

		public DatarouterStoragePlugin getSimplePluginData(){
			return new DatarouterStoragePlugin();
		}

		public DatarouterStoragePlugin build(){
			return new DatarouterStoragePlugin(
					serverType,
					serverTypeDetectorClass,
					datarouterPropertiesClass,
					settingOverridesClass,
					additionalSettingRoots,
					clientOptionsFactoryClass,
					daoClasses);
		}

	}

}
