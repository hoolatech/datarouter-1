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
package io.datarouter.gcp.spanner.op.read.index;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerGetFromIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends SpannerBaseReadIndexOp<PK,IE>{

	private final Collection<IK> keys;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;

	public SpannerGetFromIndexOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<IK> keys,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		super(client, config, codecRegistry, fieldInfo.getTableName());
		this.keys = keys;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
	}

	@Override
	public KeySet buildKeySet(){
		return buildKeySet(keys);
	}

	@Override
	public List<IE> wrappedCall(){
		List<String> columns = indexEntryFieldInfo.getFields().stream()
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.collect(Collectors.toList());
		String indexName = indexEntryFieldInfo.getIndexName();
		ResultSet rs;
		if(config.getLimit() != null){
			rs = client.singleUseReadOnlyTransaction().readUsingIndex(
					tableName,
					indexName,
					buildKeySet(),
					columns,
					Options.limit(config.getLimit()));
		}else{
			rs = client.singleUseReadOnlyTransaction().readUsingIndex(tableName, indexName, buildKeySet(), columns);
		}
		return createFromResultSet(rs, indexEntryFieldInfo.getDatabeanSupplier(), indexEntryFieldInfo.getFields());
	}

}
