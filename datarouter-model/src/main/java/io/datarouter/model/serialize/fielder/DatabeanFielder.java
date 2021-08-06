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
package io.datarouter.model.serialize.fielder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;

public interface DatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>{

	Fielder<PK> getKeyFielder();
	List<Field<?>> getFields(D fieldSet);
	List<Field<?>> getKeyFields(D databean);
	List<Field<?>> getNonKeyFields(D databean);
	Map<String,List<Field<?>>> getUniqueIndexes(D databean);
	void addOption(FielderConfigValue<?> fielderConfigValue);
	<T extends FielderConfigValue<T>> Optional<T> getOption(FielderConfigKey<T> key);
	Collection<FielderConfigValue<?>> getOptions();
	Optional<Long> getTtlMs();
	Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass();
	StringDatabeanCodec getStringDatabeanCodec();

	default void configure(){
	}

}
