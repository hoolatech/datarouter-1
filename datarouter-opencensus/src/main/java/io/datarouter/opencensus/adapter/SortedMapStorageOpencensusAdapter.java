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
package io.datarouter.opencensus.adapter;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.mixin.MapStorageReaderOpencensusAdapterMixin;
import io.datarouter.opencensus.adapter.mixin.MapStorageWriterOpencensusAdapterMixin;
import io.datarouter.opencensus.adapter.mixin.SortedStorageReaderOpencensusAdapterMixin;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;

public abstract class SortedMapStorageOpencensusAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D,F>>
extends BaseOpencensusAdapter<PK,D,F,N>
implements SortedMapStorageNode<PK,D,F>,
		MapStorageWriterOpencensusAdapterMixin<PK,D,F,N>,
		SortedStorageReaderOpencensusAdapterMixin<PK,D,F,N>,
		MapStorageReaderOpencensusAdapterMixin<PK,D,F,N>{

	public SortedMapStorageOpencensusAdapter(N backingNode){
		super(backingNode);
	}

}
