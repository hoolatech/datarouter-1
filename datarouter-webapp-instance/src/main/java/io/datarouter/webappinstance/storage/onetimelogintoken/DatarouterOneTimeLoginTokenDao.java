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
package io.datarouter.webappinstance.storage.onetimelogintoken;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.webappinstance.storage.onetimelogintoken.OneTimeLoginToken.OneTimeLoginTokenFielder;

@Singleton
public class DatarouterOneTimeLoginTokenDao extends BaseDao{

	public static class DatarouterOneTimeLoginTokenDaoParams extends BaseDaoParams{

		public DatarouterOneTimeLoginTokenDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final MapStorage<OneTimeLoginTokenKey,OneTimeLoginToken> node;

	@Inject
	public DatarouterOneTimeLoginTokenDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterOneTimeLoginTokenDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, OneTimeLoginToken::new, OneTimeLoginTokenFielder::new)
				.buildAndRegister();
	}

	public OneTimeLoginToken get(OneTimeLoginTokenKey key){
		return node.get(key);
	}

	public void put(OneTimeLoginToken oneTimeLoginToken){
		node.put(oneTimeLoginToken);
	}

	public void delete(OneTimeLoginTokenKey key){
		node.delete(key);
	}

	public void deleteAll(){
		node.deleteAll();
	}

}
