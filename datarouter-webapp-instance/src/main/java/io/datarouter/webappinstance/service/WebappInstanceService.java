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
package io.datarouter.webappinstance.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.webappinstance.WebappInstanceDto;
import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServerPrivateIp;
import io.datarouter.storage.config.properties.ServerPublicIp;
import io.datarouter.util.MxBeans;
import io.datarouter.util.SystemTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.monitoring.BuildProperties;
import io.datarouter.web.monitoring.GitProperties;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceSettingRoot;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;

@Singleton
public class WebappInstanceService{

	/*
	Sql's Timestamp adjusts the Instant to local time, converting Instant.EPOCH to '1969-12-31 16:00:00' for PST
	which is an invalid value for TIMESTAMP data type.
	TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC
	*/
	private static final Instant INSTANT_EPOCH_ADJUSTED;
	static{
		int offsetSeconds = ZoneId.systemDefault().getRules().getStandardOffset(Instant.now()).getTotalSeconds();
		INSTANT_EPOCH_ADJUSTED = Instant.EPOCH.minusSeconds(offsetSeconds).plusSeconds(1L);
	}

	private final DatarouterWebappInstanceDao webappInstanceDao;
	private final DatarouterWebappInstanceLogDao webappInstanceLogDao;
	private final WebappName webappName;
	private final GitProperties gitProperties;
	private final BuildProperties buildProperties;
	private final ServletContextSupplier servletContext;
	private final DatarouterWebappInstanceSettingRoot settings;
	private final WebappInstancePublisher webappInstancePublisher;
	private final CompoundPortIdentifier portIdentifier;
	private final ServerName serverName;
	private final ServerPublicIp serverPublicIp;
	private final ServerPrivateIp serverPrivateIp;
	private final DatarouterServerTypeSupplier serverType;

	private final Instant startup;

	@Inject
	public WebappInstanceService(
			DatarouterWebappInstanceDao webappInstanceDao,
			DatarouterWebappInstanceLogDao webappInstanceLogDao,
			WebappName webappName,
			GitProperties gitProperties,
			BuildProperties buildProperties,
			ServletContextSupplier servletContext,
			WebappInstancePublisher webappInstancePublisher,
			DatarouterWebappInstanceSettingRoot settings,
			CompoundPortIdentifier portIdentifier,
			ServerName serverName,
			ServerPublicIp serverPublicIp,
			ServerPrivateIp serverPrivateIp,
			DatarouterServerTypeSupplier serverType){
		this.webappInstanceDao = webappInstanceDao;
		this.webappInstanceLogDao = webappInstanceLogDao;
		this.webappName = webappName;
		this.gitProperties = gitProperties;
		this.buildProperties = buildProperties;
		this.servletContext = servletContext;
		this.webappInstancePublisher = webappInstancePublisher;
		this.settings = settings;
		this.portIdentifier = portIdentifier;
		this.serverName = serverName;
		this.serverPublicIp = serverPublicIp;
		this.serverPrivateIp = serverPrivateIp;
		this.serverType = serverType;

		this.startup = Instant.ofEpochMilli(MxBeans.RUNTIME.getStartTime());
	}

	public List<WebappInstance> getAll(){
		return webappInstanceDao.scan().list();
	}

	public WebappInstance updateWebappInstanceTable(){
		String buildId = buildProperties.getBuildId();
		String commitId = gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING);
		Counters.inc("App heartbeat " + serverType.getServerTypeString());
		Counters.inc("App heartbeat type-build " + serverType.getServerTypeString() + " " + buildId);
		Counters.inc("App heartbeat type-commit " + serverType.getServerTypeString() + " " + commitId);
		Counters.inc("App heartbeat build " + buildId);
		Counters.inc("App heartbeat commit " + commitId);
		WebappInstance webappInstance = buildCurrentWebappInstance();
		webappInstanceDao.put(webappInstance);
		webappInstanceLogDao.put(new WebappInstanceLog(webappInstance));
		if(settings.webappInstancePublisher.get()){
			WebappInstanceDto dto = webappInstance.toDto();
			webappInstancePublisher.add(dto);
		}
		return webappInstance;
	}

	public WebappInstance buildCurrentWebappInstance(){
		return new WebappInstance(
				webappName.getName(),
				serverName.get(),
				serverType.getServerTypeString(),
				servletContext.get().getContextPath(),
				serverPublicIp.get(),
				serverPrivateIp.get(),
				startup,
				gitProperties.getBuildTime().orElse(INSTANT_EPOCH_ADJUSTED),
				buildProperties.getBuildId(),
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				SystemTool.getJavaVersion(),
				servletContext.get().getServerInfo(),
				gitProperties.getBranch().orElse(GitProperties.UNKNOWN_STRING),
				portIdentifier.getHttpsPort());
	}

	public WebappInstanceKey buildCurrentWebappInstanceKey(){
		return new WebappInstanceKey(webappName.getName(), serverName.get());
	}

}
