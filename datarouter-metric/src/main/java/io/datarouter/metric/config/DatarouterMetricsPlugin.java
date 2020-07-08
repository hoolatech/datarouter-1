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
package io.datarouter.metric.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.count.CountPublisher.NoOpCountPublisher;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.gauge.GaugePublisher.NoOpGaugePublisher;
import io.datarouter.metric.MetricNameRegistry;
import io.datarouter.metric.counter.CountersAppListener;
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.metric.counter.DatarouterCountPublisherDao.DatarouterCountPublisherDaoParams;
import io.datarouter.metric.counter.conveyor.CountConveyors;
import io.datarouter.metric.metric.DatabeanGauges;
import io.datarouter.metric.metric.DatarouterGaugePublisherDao;
import io.datarouter.metric.metric.DatarouterGaugePublisherDao.DatarouterGaugePublisherDaoParams;
import io.datarouter.metric.metric.conveyor.GaugeConveyors;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterMetricsPlugin extends BaseWebPlugin{

	private static final DatarouterMetricPaths PATHS = new DatarouterMetricPaths();

	private final Class<? extends CountPublisher> countPublisher;
	private final Class<? extends GaugePublisher> gaugePublisher;
	private final List<String> metricNames;

	private DatarouterMetricsPlugin(
			DatarouterMetricsDaosModule daosModuleBuilder,
			Class<? extends CountPublisher> countPublisher,
			Class<? extends GaugePublisher> gaugePublisher,
			boolean enableCountPublishing,
			boolean enableGaugePublishing,
			List<String> metricNames){
		this.countPublisher = countPublisher;
		this.gaugePublisher = gaugePublisher;
		this.metricNames = metricNames;

		if(enableCountPublishing){
			addAppListener(CountConveyors.class);
			addAppListener(CountersAppListener.class);
			addSettingRoot(DatarouterCountSettingRoot.class);
		}
		if(enableGaugePublishing){
			addAppListener(GaugeConveyors.class);
			addSettingRoot(DatarouterGaugeSettingRoot.class);
		}

		addRouteSet(DatarouterMetricRouteSet.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.metric.viewMetricNames, "Metric Names");
		setDaosModule(daosModuleBuilder);
	}

	@Override
	public String getName(){
		return "DatarouterMetrics";
	}

	@Override
	public void configure(){
		bind(CountPublisher.class).to(countPublisher);
		bind(GaugePublisher.class).to(gaugePublisher);
		bindActual(Gauges.class, DatabeanGauges.class);
		bindActualInstance(MetricNameRegistry.class, new MetricNameRegistry(metricNames));
	}

	public static class DatarouterMetricsPluginBuilder{

		private final ClientId defaultQueueClientId;
		private final List<String> metricNames;

		private Class<? extends CountPublisher> countPublisher = NoOpCountPublisher.class;
		private Class<? extends GaugePublisher> gaugePublisher = NoOpGaugePublisher.class;

		private DatarouterMetricsDaosModule daosModule;

		public DatarouterMetricsPluginBuilder(
				ClientId defaultQueueClientId,
				Class<? extends CountPublisher> countPublisher,
				Class<? extends GaugePublisher> gaugePublisher){
			this.defaultQueueClientId = defaultQueueClientId;
			this.countPublisher = countPublisher;
			this.gaugePublisher = gaugePublisher;
			this.metricNames = new ArrayList<>();
		}

		public DatarouterMetricsPluginBuilder withDaosModule(DatarouterMetricsDaosModule daosModule){
			this.daosModule = daosModule;
			return this;
		}

		public DatarouterMetricsPluginBuilder withCountPublisher(Class<? extends CountPublisher> countPublisher){
			this.countPublisher = countPublisher;
			return this;
		}

		public DatarouterMetricsPluginBuilder withGaugePublisher(Class<? extends GaugePublisher> gaugePublisher){
			this.gaugePublisher = gaugePublisher;
			return this;
		}

		public DatarouterMetricsPluginBuilder addMetricNames(List<String> names){
			this.metricNames.addAll(names);
			return this;
		}

		public DatarouterMetricsPlugin build(){
			boolean enableCountPublishing = !countPublisher.isInstance(NoOpCountPublisher.class);
			boolean enableGaugePublishing = !gaugePublisher.isInstance(NoOpGaugePublisher.class);

			return new DatarouterMetricsPlugin(
					daosModule == null
							? new DatarouterMetricsDaosModule(defaultQueueClientId, enableCountPublishing,
									enableGaugePublishing)
							: daosModule,
					countPublisher,
					gaugePublisher,
					enableCountPublishing,
					enableGaugePublishing,
					metricNames);
		}

	}

	public static class DatarouterMetricsDaosModule extends DaosModuleBuilder{

		private final ClientId datarouterCountPublisherClientId;
		private final ClientId datarouterGaugePublisherClientId;

		private final boolean enableCountPublishing;
		private final boolean enableGaugePublishing;

		public DatarouterMetricsDaosModule(ClientId defaultQueueClientId, boolean countPublishing,
				boolean gaugePublishing){
			this(defaultQueueClientId, defaultQueueClientId, countPublishing, gaugePublishing);
		}

		public DatarouterMetricsDaosModule(
				ClientId datarouterCountPublisherClientId,
				ClientId datarouterGaugePublisherClientId,
				boolean enableCountPublishing,
				boolean enableGaugePublishing){
			this.datarouterCountPublisherClientId = datarouterCountPublisherClientId;
			this.datarouterGaugePublisherClientId = datarouterGaugePublisherClientId;
			this.enableCountPublishing = enableCountPublishing;
			this.enableGaugePublishing = enableGaugePublishing;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableCountPublishing){
				daos.add(DatarouterCountPublisherDao.class);
			}
			if(enableGaugePublishing){
				daos.add(DatarouterGaugePublisherDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableCountPublishing){
				bind(DatarouterCountPublisherDaoParams.class).toInstance(new DatarouterCountPublisherDaoParams(
						datarouterCountPublisherClientId));
			}
			if(enableGaugePublishing){
				bind(DatarouterGaugePublisherDaoParams.class).toInstance(new DatarouterGaugePublisherDaoParams(
						datarouterGaugePublisherClientId));
			}
		}

	}

}
