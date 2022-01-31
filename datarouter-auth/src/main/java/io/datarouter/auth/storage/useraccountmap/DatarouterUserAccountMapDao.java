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
package io.datarouter.auth.storage.useraccountmap;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap.DatarouterUserAccountMapFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterUserAccountMapDao extends BaseDao implements BaseDatarouterUserAccountMapDao{

	public static class DatarouterUserAccountMapDaoParams extends BaseRedundantDaoParams{

		public DatarouterUserAccountMapDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<DatarouterUserAccountMapKey,DatarouterUserAccountMap,
			DatarouterUserAccountMapFielder> node;

	@Inject
	public DatarouterUserAccountMapDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterUserAccountMapDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<DatarouterUserAccountMapKey,DatarouterUserAccountMap,
					DatarouterUserAccountMapFielder> node =
							nodeFactory.create(clientId, DatarouterUserAccountMap::new,
									DatarouterUserAccountMapFielder::new)
						.withIsSystemTable(true)
						.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	@Override
	public void deleteMulti(Collection<DatarouterUserAccountMapKey> keys){
		node.deleteMulti(keys);
	}

	@Override
	public void put(DatarouterUserAccountMap databean){
		node.put(databean);
	}

	@Override
	public void putMulti(Collection<DatarouterUserAccountMap> databeans){
		node.putMulti(databeans);
	}

	@Override
	public Scanner<DatarouterUserAccountMapKey> scanKeysWithPrefix(DatarouterUserAccountMapKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	@Override
	public boolean exists(DatarouterUserAccountMapKey key){
		return node.exists(key);
	}

	public Scanner<DatarouterUserAccountMapKey> scanKeys(){
		return node.scanKeys();
	}

}
