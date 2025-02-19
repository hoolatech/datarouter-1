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
package io.datarouter.web.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.inject.InstanceRegistry;

@Singleton
public class HttpClientAppListener implements DatarouterAppListener{

	@Inject
	private InstanceRegistry instanceRegistry;

	@Override
	public void onShutDown(){
		for(DatarouterHttpClient httpClient : instanceRegistry.getAllInstancesOfType(DatarouterHttpClient.class)){
			httpClient.shutdown();
		}
	}

	@Override
	public boolean safeToExecuteInParallel(){
		return false;
	}

}
