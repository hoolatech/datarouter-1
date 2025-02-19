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
package io.datarouter.exception.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.exception.conveyors.ExceptionQueueConveyors;
import io.datarouter.exception.filter.GuiceExceptionHandlingFilter;
import io.datarouter.exception.service.DefaultExceptionHandlingConfig;
import io.datarouter.exception.service.DefaultExceptionRecorder;
import io.datarouter.exception.service.ExceptionGraphLink;
import io.datarouter.exception.service.ExceptionGraphLink.NoOpExceptionGraphLink;
import io.datarouter.exception.service.ExceptionIssueLinkPrefixSupplier;
import io.datarouter.exception.service.ExceptionIssueLinkPrefixSupplier.ExceptionIssueLinkPrefix;
import io.datarouter.exception.service.ExceptionIssueLinkPrefixSupplier.NoOpExceptionIssueLinkPrefixSupplier;
import io.datarouter.exception.service.ExceptionRecordAggregationDailyDigest;
import io.datarouter.exception.service.ExemptDailyDigestExceptions;
import io.datarouter.exception.service.ExemptDailyDigestExceptions.NoOpExemptDailyDigestExceptions;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao.DatarouterExceptionRecordDaoParams;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordPublisherDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordPublisherDao.DatarouterExceptionPublisherRouterParams;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao.DatarouterHttpRequestRecordDaoParams;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordPublisherDao;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordPublisherDao.DatarouterHttpRequestRecordPublisherDaoParams;
import io.datarouter.exception.storage.metadata.DatarouterExceptionRecordSummaryMetadataDao;
import io.datarouter.exception.storage.metadata.DatarouterExceptionRecordSummaryMetadataDao.DatarouterExceptionRecordSummaryMetadataDaoParams;
import io.datarouter.exception.storage.summary.DatarouterExceptionRecordSummaryDao;
import io.datarouter.exception.storage.summary.DatarouterExceptionRecordSummaryDao.DatarouterExceptionRecordSummaryDaoParams;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher.NoOpExceptionRecordPublisher;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.config.DatarouterWebPlugin;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterExceptionPlugin extends BaseWebPlugin{

	private final Class<? extends ExceptionGraphLink> exceptionGraphLinkClass;
	private final Class<? extends ExceptionRecorder> exceptionRecorderClass;
	private final Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
	private final Class<? extends ExceptionRecordPublisher> exceptionRecordPublisher;
	private final Class<? extends ExemptDailyDigestExceptions> exemptDailyDigestExceptions;
	private final String issueLinkPrefix;

	private DatarouterExceptionPlugin(DatarouterExceptionDaoModule daosModuleBuilder,
			Class<? extends ExceptionGraphLink> exceptionGraphLinkClass,
			Class<? extends ExceptionRecorder> exceptionRecorderClass,
			Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass,
			Class<? extends ExceptionRecordPublisher> exceptionRecordPublisher,
			Class<? extends ExemptDailyDigestExceptions> exemptDailyDigestExceptions,
			String issueLinkPrefix){
		this.exceptionGraphLinkClass = exceptionGraphLinkClass;
		this.exceptionRecorderClass = exceptionRecorderClass;
		this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
		this.exceptionRecordPublisher = exceptionRecordPublisher;
		this.exemptDailyDigestExceptions = exemptDailyDigestExceptions;
		this.issueLinkPrefix = issueLinkPrefix;
		addFilterParamsOrdered(
				new FilterParams(
						false,
						DatarouterServletGuiceModule.ROOT_PATH,
						GuiceExceptionHandlingFilter.class,
						FilterParamGrouping.DATAROUTER),
				DatarouterWebPlugin.REQUEST_CACHING_FILTER_PARAMS);
		addRouteSet(DatarouterExceptionRouteSet.class);
		addSettingRoot(DatarouterExceptionSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterExceptionTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.MONITORING,
				new DatarouterExceptionPaths().datarouter.exception.browse,
				"Exceptions");
		addDatarouterGithubDocLink("datarouter-exception");
		if(!exceptionRecordPublisher.isInstance(NoOpExceptionRecordPublisher.class)){
			addAppListener(ExceptionQueueConveyors.class);
		}
		addDailyDigest(ExceptionRecordAggregationDailyDigest.class);
	}

	@Override
	protected void configure(){
		bindDefault(ExceptionGraphLink.class, exceptionGraphLinkClass);
		bindActual(ExceptionRecorder.class, exceptionRecorderClass);
		bindActual(ExceptionHandlingConfig.class, exceptionHandlingConfigClass);
		bind(ExceptionRecordPublisher.class).to(exceptionRecordPublisher);
		bind(ExemptDailyDigestExceptions.class).to(exemptDailyDigestExceptions);
		if(issueLinkPrefix == null){
			bind(ExceptionIssueLinkPrefixSupplier.class).to(NoOpExceptionIssueLinkPrefixSupplier.class);
		}else{
			bindDefaultInstance(ExceptionIssueLinkPrefixSupplier.class, new ExceptionIssueLinkPrefix(issueLinkPrefix));
		}
	}

	public static class DatarouterExceptionPluginBuilder{

		private final List<ClientId> defaultClientIds;

		private List<ClientId> defaultQueueClientId;
		private Class<? extends ExceptionGraphLink> exceptionGraphLinkClass = NoOpExceptionGraphLink.class;
		private Class<? extends ExceptionRecorder> exceptionRecorderClass = DefaultExceptionRecorder.class;
		private Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass
				= DefaultExceptionHandlingConfig.class;
		private Class<? extends ExceptionRecordPublisher> exceptionRecordPublisher = NoOpExceptionRecordPublisher.class;
		private Class<? extends ExemptDailyDigestExceptions> exemptDailyDigestExceptions
				= NoOpExemptDailyDigestExceptions.class;
		private String issueLinkPrefix;

		public DatarouterExceptionPluginBuilder(List<ClientId> defaultClientIds){
			this.defaultClientIds = defaultClientIds;
		}

		public DatarouterExceptionPluginBuilder setExceptionGraphLinkClass(
				Class<? extends ExceptionGraphLink> exceptionGraphLinkClass){
			this.exceptionGraphLinkClass = exceptionGraphLinkClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExceptionRecorderClass(
				Class<? extends ExceptionRecorder> exceptionRecorderClass){
			this.exceptionRecorderClass = exceptionRecorderClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExceptionHandlingClass(
				Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass){
			this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExemptDailyDigestExceptions(
				Class<? extends ExemptDailyDigestExceptions> exemptDailyDigestExceptions){
			this.exemptDailyDigestExceptions = exemptDailyDigestExceptions;
			return this;
		}

		public DatarouterExceptionPluginBuilder enablePublishing(
				Class<? extends ExceptionRecordPublisher> exceptionRecordPublisher,
				List<ClientId> defaultQueueClientId){
			this.exceptionRecordPublisher = exceptionRecordPublisher;
			this.defaultQueueClientId = defaultQueueClientId;
			return this;
		}

		public DatarouterExceptionPluginBuilder withIssueLinkPrefix(String issueLinkPrefix){
			this.issueLinkPrefix = issueLinkPrefix;
			return this;
		}

		public DatarouterExceptionPlugin build(){
			return new DatarouterExceptionPlugin(
					new DatarouterExceptionDaoModule(defaultClientIds, defaultQueueClientId),
					exceptionGraphLinkClass,
					exceptionRecorderClass,
					exceptionHandlingConfigClass,
					exceptionRecordPublisher,
					exemptDailyDigestExceptions,
					issueLinkPrefix);
		}

	}

	public static class DatarouterExceptionDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterExceptionRecordClientId;
		private final List<ClientId> datarouterExceptionRecordSummaryClientId;
		private final List<ClientId> datarouterExceptionRecordSummaryMetadataClientId;
		private final List<ClientId> datarouterHttpRequestRecordClientId;

		private final List<ClientId> datarouterExceptionRecordPublisherClientId;
		private final List<ClientId> datarouterHttpRequestRecordPublisherClientId;

		public DatarouterExceptionDaoModule(List<ClientId> defaultClientIds, List<ClientId> queueClientId){
			this(defaultClientIds, defaultClientIds, defaultClientIds, defaultClientIds,
					queueClientId, queueClientId);
		}

		public DatarouterExceptionDaoModule(
				List<ClientId> datarouterExceptionRecordClientId,
				List<ClientId> datarouterExceptionRecordSummaryClientId,
				List<ClientId> datarouterExceptionRecordSummaryMetadataClientId,
				List<ClientId> datarouterHttpRequestRecordClientId,
				List<ClientId> datarouterExceptionRecordPublisherClientId,
				List<ClientId> datarouterHttpRequestRecordPublisherClientId){
			this.datarouterExceptionRecordClientId = datarouterExceptionRecordClientId;
			this.datarouterExceptionRecordSummaryClientId = datarouterExceptionRecordSummaryClientId;
			this.datarouterExceptionRecordSummaryMetadataClientId = datarouterExceptionRecordSummaryMetadataClientId;
			this.datarouterHttpRequestRecordClientId = datarouterHttpRequestRecordClientId;

			this.datarouterExceptionRecordPublisherClientId = datarouterExceptionRecordPublisherClientId;
			this.datarouterHttpRequestRecordPublisherClientId = datarouterHttpRequestRecordPublisherClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			daos.add(DatarouterExceptionRecordDao.class);
			daos.add(DatarouterExceptionRecordSummaryDao.class);
			daos.add(DatarouterExceptionRecordSummaryMetadataDao.class);
			daos.add(DatarouterHttpRequestRecordDao.class);
			if(datarouterExceptionRecordPublisherClientId != null){
				daos.add(DatarouterExceptionRecordPublisherDao.class);
			}
			if(datarouterHttpRequestRecordPublisherClientId != null){
				daos.add(DatarouterHttpRequestRecordPublisherDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			bind(DatarouterHttpRequestRecordDaoParams.class)
					.toInstance(new DatarouterHttpRequestRecordDaoParams(datarouterHttpRequestRecordClientId));
			bind(DatarouterExceptionRecordDaoParams.class)
					.toInstance(new DatarouterExceptionRecordDaoParams(datarouterExceptionRecordClientId));
			bind(DatarouterExceptionRecordSummaryDaoParams.class)
					.toInstance(new DatarouterExceptionRecordSummaryDaoParams(
							datarouterExceptionRecordSummaryClientId));
			bind(DatarouterExceptionRecordSummaryMetadataDaoParams.class)
					.toInstance(new DatarouterExceptionRecordSummaryMetadataDaoParams(
							datarouterExceptionRecordSummaryMetadataClientId));

			if(datarouterExceptionRecordPublisherClientId != null){
				bind(DatarouterExceptionPublisherRouterParams.class)
						.toInstance(new DatarouterExceptionPublisherRouterParams(
								datarouterExceptionRecordPublisherClientId));
			}
			if(datarouterHttpRequestRecordPublisherClientId != null){
				bind(DatarouterHttpRequestRecordPublisherDaoParams.class)
						.toInstance(new DatarouterHttpRequestRecordPublisherDaoParams(
								datarouterHttpRequestRecordPublisherClientId));
			}
		}

	}

}
