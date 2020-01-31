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
package io.datarouter.auth.cache;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.util.cache.LoadingCache.LoadingCacheBuilder;
import io.datarouter.util.cache.LoadingCacheWrapper;

@Singleton
public class DatarouterAccountByApiKeyCache extends LoadingCacheWrapper<String,DatarouterAccount>{

	@Inject
	public DatarouterAccountByApiKeyCache(BaseDatarouterAccountDao dao){
		super(new LoadingCacheBuilder<String,DatarouterAccount>()
				.withLoadingFunction(key -> {
					return dao.scan()
						.include(account -> account.getApiKey().equals(key))
						.findAny()
						.orElse(null);
				})
				.withExpireTtl(Duration.ofSeconds(6L))
				.build());
	}

}
