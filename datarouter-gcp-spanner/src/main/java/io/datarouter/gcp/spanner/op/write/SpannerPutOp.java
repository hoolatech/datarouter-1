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
package io.datarouter.gcp.spanner.op.write;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Mutation.WriteBuilder;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.util.SpannerEntityKeyTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
		extends SpannerBaseWriteOp<D>{

	protected final Config config;
	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	protected final SpannerFieldCodecRegistry codecRegistry;

	public SpannerPutOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<D> databeans,
			Config config,
			SpannerFieldCodecRegistry codecRegistry){
		this(client, fieldInfo, databeans, config, codecRegistry, fieldInfo.getTableName());
	}

	protected SpannerPutOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<D> databeans,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			String tableName){
		super(client, tableName, config, databeans);
		this.config = config;
		this.fieldInfo = fieldInfo;
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Collection<Mutation> getMutations(){
		return Scanner.of(values).map(this::databeanToMutation).list();

	}

	protected WriteBuilder getMutationPartition(@SuppressWarnings("unused") D databean, WriteBuilder mutation){
		return mutation;
	}

	protected Mutation databeanToMutation(D databean){
		WriteBuilder mutation;
		switch(config.getPutMethod()){
		case INSERT_OR_BUST:
			mutation = Mutation.newInsertBuilder(tableName);
			break;
		case UPDATE_OR_BUST:
			mutation = Mutation.newUpdateBuilder(tableName);
			break;
		default:
			mutation = Mutation.newInsertOrUpdateBuilder(tableName);
		}
		mutation = getMutationPartition(databean, mutation);
		List<? extends SpannerBaseFieldCodec<?,?>> primaryKeyCodecs = codecRegistry.createCodecs(SpannerEntityKeyTool
				.getPrimaryKeyFields(databean.getKey(), fieldInfo.isSubEntity()));
		List<? extends SpannerBaseFieldCodec<?,?>> nonKeyCodecs = codecRegistry.createCodecs(fieldInfo
				.getNonKeyFieldsWithValues(databean));
		for(SpannerBaseFieldCodec<?,?> codec : primaryKeyCodecs){
			if(codec.getField().getValue() != null){
				mutation = codec.setMutation(mutation);
			}
		}
		for(SpannerBaseFieldCodec<?,?> codec : nonKeyCodecs){
			if(codec.getField().getValue() != null){
				mutation = codec.setMutation(mutation);
			}
		}
		return mutation.build();
	}

}
