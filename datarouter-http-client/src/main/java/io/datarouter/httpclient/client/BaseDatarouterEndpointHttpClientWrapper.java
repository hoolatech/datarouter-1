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
package io.datarouter.httpclient.client;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.EndpointRegistry;
import io.datarouter.httpclient.response.Conditional;

public abstract class BaseDatarouterEndpointHttpClientWrapper<
		T extends BaseEndpoint<?>,
		R extends EndpointRegistry>
extends BaseDatarouterHttpClientWrapper{

	public final R endpoints;

	public BaseDatarouterEndpointHttpClientWrapper(DatarouterHttpClient datarouterHttpClient, R endpoints){
		super(datarouterHttpClient);
		this.endpoints = endpoints;
	}

	public <E,A extends BaseEndpoint<E>> Conditional<E> call(A endpoint){
		return super.tryExecute(endpoint);
	}

}
