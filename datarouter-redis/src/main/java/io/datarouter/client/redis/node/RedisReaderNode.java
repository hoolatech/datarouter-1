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
package io.datarouter.client.redis.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.TallyStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.collection.CollectionTool;
import redis.clients.jedis.Jedis;

public class RedisReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, TallyStorageReader<PK,D>{

	private final Integer databeanVersion;
	private final RedisClientManager redisClientManager;
	private final ClientId clientId;

	public RedisReaderNode(
			NodeParams<PK,D,F> params,
			RedisClientType redisClientType,
			RedisClientManager redisClientManager,
			ClientId clientId){
		super(params, redisClientType);
		this.redisClientManager = redisClientManager;
		this.clientId = clientId;
		this.databeanVersion = Objects.requireNonNull(params.getSchemaVersion());
	}

	@Override
	public boolean exists(PK key, Config config){
		return getClient().exists(buildRedisKey(key));
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		String json = getClient().get(buildRedisKey(key));
		if(json == null){
			return null;
		}
		return JsonDatabeanTool.databeanFromJson(getFieldInfo().getDatabeanSupplier(),
				getFieldInfo().getSampleFielder(), json);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		return getClient().mget(buildRedisKeys(keys).toArray(new String[keys.size()])).stream()
				.filter(Objects::nonNull)
				.map(bean -> JsonDatabeanTool.databeanFromJson(getFieldInfo().getDatabeanSupplier(), getFieldInfo()
						.getSampleFielder(), bean))
				.collect(Collectors.toList());
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		return DatabeanTool.getKeys(getMulti(keys, config));
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		if(key == null){
			return null;
		}
		return Optional.ofNullable(getClient().get(buildRedisKey(new TallyKey(key))))
				.map(String::trim)
				.map(Long::valueOf);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		return keys.stream()
				.collect(Collectors.toMap(Function.identity(), key -> findTallyCount(key).orElse(0L)));
	}

	protected String buildRedisKey(PrimaryKey<?> pk){
		return new RedisEncodedKey(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildRedisKeys(Collection<? extends PrimaryKey<?>> pks){
		return RedisEncodedKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

	protected Jedis getClient(){
		return redisClientManager.getJedis(clientId);
	}

}
