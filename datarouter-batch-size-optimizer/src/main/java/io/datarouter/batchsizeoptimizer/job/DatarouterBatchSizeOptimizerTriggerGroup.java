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
package io.datarouter.batchsizeoptimizer.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.batchsizeoptimizer.config.DatarouterBatchSizeOptimizerSettings;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.util.time.ZoneIds;

@Singleton
public class DatarouterBatchSizeOptimizerTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterBatchSizeOptimizerTriggerGroup(DatarouterBatchSizeOptimizerSettings batchSizeOptimizerSettings){
		super("DatarouterBatchSizeOptimizer", true, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				"24 * * * * ?",
				batchSizeOptimizerSettings.runOpPerformanceRecordAggregationJob,
				OpPerformanceRecordAggregationJob.class,
				true);
		registerLocked(
				"53 4/5 * * * ?",
				batchSizeOptimizerSettings.runBatchSizeOptimizingJob,
				BatchSizeOptimizingJob.class,
				true);
	}

}
