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
package io.datarouter.storage.node.op.raw.write;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;

public interface TallyStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends MapStorageWriter<PK,D>{

	public static final String OP_deleteTally = "deleteTally";

	void deleteTally(String key, Config config);

	default void deleteTally(String key){
		deleteTally(key, new Config());
	}

	public interface TallyStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends TallyStorageWriter<PK,D>, MapStorageWriterNode<PK,D,F>{
	}

	public interface PhysicalTallyStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends TallyStorageWriterNode<PK,D,F>, PhysicalMapStorageNode<PK,D,F>{
	}

}
