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
package io.datarouter.auth.cache;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.util.cache.LoadingCache.LoadingCacheBuilder;
import io.datarouter.util.cache.LoadingCacheWrapper;
import io.datarouter.web.exception.InvalidCredentialsException;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;

@Singleton
public class DatarouterUserByUsernameCache extends LoadingCacheWrapper<String,DatarouterUser>{

	@Inject
	public DatarouterUserByUsernameCache(DatarouterUserDao datarouterUserDao){
		super(new LoadingCacheBuilder<String,DatarouterUser>()
				.withLoadingFunction(key -> datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(key)))
				.withExpireTtl(Duration.ofSeconds(6))
				.withExceptionFunction(key -> new InvalidCredentialsException("username not found (" + key + ")"))
				.build());
	}

}
