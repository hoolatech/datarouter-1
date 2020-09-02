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

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterMetricPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final MetricPaths metric = branch(MetricPaths::new, "metric");
	}

	public static class MetricPaths extends PathNode{
		public final MetricNamePaths metricNames = branch(MetricNamePaths::new, "metricNames");
	}

	public static class MetricNamePaths extends PathNode{
		public final PathNode appHandlers = leaf("appHandlers");
		public final PathNode appJobs = leaf("appJobs");
		public final PathNode appTables = leaf("appTables");

		public final PathNode datarouterHandlers = leaf("datarouterHandlers");
		public final PathNode datarouterJobs = leaf("datarouterJobs");
		public final PathNode datarouterTables = leaf("datarouterTables");

		public final PathNode registeredNames = leaf("registeredNames");
		public final PathNode metricDashboards = leaf("metricDashboards");
		public final PathNode miscMetricLinks = leaf("miscMetricLinks");
	}

}
