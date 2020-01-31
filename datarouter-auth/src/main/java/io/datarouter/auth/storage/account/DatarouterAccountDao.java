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
package io.datarouter.auth.storage.account;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.DatarouterAccount.DatarouterAccountFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterAccountDao extends BaseDao implements BaseDatarouterAccountDao{

	public static class DatarouterAccountDaoParams extends BaseDaoParams{

		public DatarouterAccountDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<DatarouterAccountKey,DatarouterAccount> node;

	@Inject
	public DatarouterAccountDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterAccountDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, DatarouterAccount::new, DatarouterAccountFielder::new)
				.buildAndRegister();
	}

	@Override
	public void put(DatarouterAccount databean){
		node.put(databean);
	}

	@Override
	public void putMulti(Collection<DatarouterAccount> databeans){
		node.putMulti(databeans);
	}

	@Override
	public DatarouterAccount get(DatarouterAccountKey key){
		return node.get(key);
	}

	@Override
	public List<DatarouterAccount> getMulti(Collection<DatarouterAccountKey> keys){
		return node.getMulti(keys);
	}

	@Override
	public Scanner<DatarouterAccount> scan(){
		return node.scan();
	}

	@Override
	public Scanner<DatarouterAccountKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public boolean exists(DatarouterAccountKey key){
		return node.exists(key);
	}

	@Override
	public void delete(DatarouterAccountKey key){
		node.delete(key);
	}

	@Override
	public Optional<DatarouterAccount> find(DatarouterAccountKey key){
		return node.find(key);
	}

}
