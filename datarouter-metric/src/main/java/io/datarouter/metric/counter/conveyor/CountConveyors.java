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
package io.datarouter.metric.counter.conveyor;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.metric.counter.setting.DatarouterCountSettingRoot;

@Singleton
public class CountConveyors extends BaseConveyors{

	@Inject
	private DatarouterCountPublisherDao countPublisherDao;
	@Inject
	private DatarouterCountSettingRoot countSettings;
	@Inject
	private Gson gson;
	@Inject
	private CountPublisher countPublisher;

	@Override
	public void onStartUp(){
		start(new CountSqsDrainConveyor(
				"countSqsToPublisher",
				countSettings.runCountsFromSqsToPublisher,
				countPublisherDao.getGroupQueueConsumerNewQueue(),
				gson,
				countPublisher,
				countSettings.compactExceptionLoggingForConveyors),
				countSettings.drainConveyorThreadCount.get());
	}

}
