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
package io.datarouter.exception.storage.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadata.ExceptionRecordSummaryMetadataFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterExceptionRecordSummaryMetadataDao extends BaseDao{

	public static class DatarouterExceptionRecordSummaryMetadataDaoParams extends BaseDaoParams{

		public DatarouterExceptionRecordSummaryMetadataDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<ExceptionRecordSummaryMetadataKey,ExceptionRecordSummaryMetadata> node;

	@Inject
	public DatarouterExceptionRecordSummaryMetadataDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterExceptionRecordSummaryMetadataDaoParams params){
		super(datarouter);
		node = nodeFactory.create(
				params.clientId,
				ExceptionRecordSummaryMetadata::new,
				ExceptionRecordSummaryMetadataFielder::new)
				.withIsSystemTable(true)
				.buildAndRegister();
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
