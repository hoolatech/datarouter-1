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
package io.datarouter.loadtest.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.loadtest.web.LoadTestGetHandler;
import io.datarouter.loadtest.web.LoadTestInsertHandler;
import io.datarouter.loadtest.web.LoadTestScanHandler;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterLoadTestRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterLoadTestRouteSet(DatarouterLoadTestPaths paths){
		super(paths.datarouter.loadTest);
		handle(paths.datarouter.loadTest.get).withHandler(LoadTestGetHandler.class);
		handle(paths.datarouter.loadTest.insert).withHandler(LoadTestInsertHandler.class);
		handle(paths.datarouter.loadTest.scan).withHandler(LoadTestScanHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.DATAROUTER_TOOLS)
				.withIsSystemDispatchRule(true);
	}

}
