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
package io.datarouter.exception.storage.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadata.ExceptionRecordSummaryMetadataFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterExceptionRecordSummaryMetadataDao extends BaseDao{

	public static class DatarouterExceptionRecordSummaryMetadataDaoParams extends BaseRedundantDaoParams{

		public DatarouterExceptionRecordSummaryMetadataDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ExceptionRecordSummaryMetadataKey,ExceptionRecordSummaryMetadata,
			ExceptionRecordSummaryMetadataFielder> node;

	@Inject
	public DatarouterExceptionRecordSummaryMetadataDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterExceptionRecordSummaryMetadataDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ExceptionRecordSummaryMetadataKey,ExceptionRecordSummaryMetadata,
							ExceptionRecordSummaryMetadataFielder> node =
							nodeFactory.create(clientId, ExceptionRecordSummaryMetadata::new,
									ExceptionRecordSummaryMetadataFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public ExceptionRecordSummaryMetadata get(ExceptionRecordSummaryMetadataKey key){
		return node.get(key);
	}

	public List<ExceptionRecordSummaryMetadata> getMulti(Collection<ExceptionRecordSummaryMetadataKey> keys){
		return node.getMulti(keys);
	}

	public void put(ExceptionRecordSummaryMetadata databean){
		node.put(databean);
	}

	public Optional<ExceptionRecordSummaryMetadata> find(ExceptionRecordSummaryMetadataKey key){
		return node.find(key);
	}

}
