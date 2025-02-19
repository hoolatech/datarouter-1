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
package io.datarouter.storage.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterClientFactoryExecutor;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.storage.config.properties.InternalConfigDirectory;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;

/**
 * Clients is a registry or cache of all clients in a Datarouter. Clients are expensive to create, so we reuse them for
 * the life of the application. The Clients class also provides a lazy-initialization feature that defers connection
 * creation, authentication, and connection pool warm-up until an application request triggers it.
 *
 * This class can be used for Datarouter management, such as displaying a web page listing all clients.
 */
@Singleton
public class DatarouterClients{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterClients.class);

	private final ClientTypeRegistry clientTypeRegistry;
	private final DatarouterClientFactoryExecutor executorService;
	private final DatarouterInjector datarouterInjector;
	private final ClientOptions clientOptions;
	private final ClientInitializationTracker clientInitializationTracker;
	private final ClientOptionsFactory clientOptionsFactory;

	private final Set<String> configFilePaths;
	private final Map<String,ClientId> clientIdByClientName;

	/*---------------------------- constructors ---------------------------- */

	@Inject
	public DatarouterClients(
			DatarouterTestPropertiesFile testPropertiesFile,
			ClientTypeRegistry clientTypeRegistry,
			DatarouterClientFactoryExecutor executorService,
			DatarouterInjector datarouterInjector,
			ClientOptions clientOptions,
			ClientInitializationTracker clientInitializationTracker,
			ClientOptionsFactory clientOptionsFactory,
			InternalConfigDirectory internalConfigDirectory){
		this.clientTypeRegistry = clientTypeRegistry;
		this.executorService = executorService;
		this.datarouterInjector = datarouterInjector;
		this.clientOptions = clientOptions;
		this.clientInitializationTracker = clientInitializationTracker;
		this.clientOptionsFactory = clientOptionsFactory;
		this.configFilePaths = new TreeSet<>();
		this.clientIdByClientName = new TreeMap<>();
		loadClientOptions(testPropertiesFile.getFileLocation(), internalConfigDirectory.get());
	}

	private void loadClientOptions(String configFilePath, String internalConfigDirectoryType){
		Properties properties = clientOptionsFactory.getInternalConfigDirectoryTypeOptions(internalConfigDirectoryType);
		boolean useClientOptionsFactory = configFilePath.isEmpty() || !properties.isEmpty();
		if(useClientOptionsFactory){
			logger.warn("Got client properties from class {}", clientOptionsFactory.getClass().getCanonicalName());
			clientOptions.addProperties(properties);
		}else if(StringTool.notEmpty(configFilePath) && !configFilePaths.contains(configFilePath)){
			configFilePaths.add(configFilePath);
			Pair<Properties,URL> propertiesAndLocation = PropertiesTool.parseAndGetLocation(configFilePath);
			logger.warn("Got client properties from file {}", propertiesAndLocation.getRight());
			clientOptions.addProperties(propertiesAndLocation.getLeft());
		}
	}

	public List<ClientId> registerClientIds(Collection<ClientId> clientIdsToAdd){
		clientIdsToAdd.forEach(clientId -> clientIdByClientName.put(clientId.getName(), clientId));
		return clientIdsToAdd.stream()
				.filter(clientInitializationTracker::isInitialized)
				.collect(Collectors.toList());
	}

	/*----------------------------- initialize ----------------------------- */

	public void initializeEagerClients(){
		initClientsInParallel(getClientNamesRequiringEagerInitialization());
	}

	public ClientType<?,?> getClientTypeInstance(ClientId clientId){
		String clientTypeName = clientOptions.getClientType(clientId);
		Objects.requireNonNull(clientTypeName, "clientType not found for clientName=" + clientId.getName());
		ClientType<?,?> clientType = clientTypeRegistry.get(clientTypeName);
		Objects.requireNonNull(clientType, "implementation not found for client type=" + clientTypeName);
		return clientType;
	}

	public ClientManager getClientManager(ClientId clientId){
		return datarouterInjector.getInstance(getClientTypeInstance(clientId).getClientManagerClass());
	}

	/*------------------------------ shutdown ------------------------------ */

	//TODO shutdown clients in parallel
	public void shutdown(){
		for(ClientId clientId : clientInitializationTracker.getInitializedClients()){
			ClientManager clientManager = getClientManager(clientId);
			try{
				long start = System.currentTimeMillis();
				clientManager.shutdown(clientId);
				logger.warn("finished shutting down client={} duration={}", clientId.getName(), DatarouterDuration
						.ageMs(start));
			}catch(Exception e){
				logger.warn("swallowing exception while shutting down client=" + clientId, e);
			}
		}
	}


	/*------------------------------ getNames ------------------------------ */

	private Collection<ClientId> getClientNamesRequiringEagerInitialization(){
		return Scanner.of(getClientIds())
				.include(clientId -> clientOptions.getInitMode(clientId, ClientInitMode.lazy) == ClientInitMode.eager)
				.list();
	}


	/*---------------------- access connection pools  ---------------------- */

	public ClientId getClientId(String clientName){
		return clientIdByClientName.get(clientName);
	}

	public Collection<ClientId> getClientIds(){
		return clientIdByClientName.values();
	}

	public Map<Boolean,List<ClientId>> getClientNamesByInitialized(){
		return Scanner.of(getClientIds())
				.groupBy(clientInitializationTracker::isInitialized);
	}

	public void initAllClients(){
		initClientsInParallel(getClientIds());
	}

	private void initClientsInParallel(Collection<ClientId> clientIds){
		List<Future<?>> futures = new ArrayList<>();
		for(ClientId clientId : clientIds){
			futures.add(executorService.submit(() -> getClientManager(clientId).initClient(clientId)));
		}
		futures.forEach(FutureTool::get);
	}

}
