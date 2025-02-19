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
package io.datarouter.virtualnode.redundant;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.virtualnode.redundant.base.BaseRedundantNode;
import io.datarouter.virtualnode.redundant.mixin.RedundantIndexedStorageMixin;
import io.datarouter.virtualnode.redundant.mixin.RedundantMapStorageMixin;
import io.datarouter.virtualnode.redundant.mixin.RedundantSortedStorageMixin;

public class RedundantIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D,F>>
extends BaseRedundantNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D,F>,
		RedundantMapStorageMixin<PK,D,F,N>,
		RedundantSortedStorageMixin<PK,D,F,N>,
		RedundantIndexedStorageMixin<PK,D,F,N>{

	/**
	 * @param nodes reads go to first node while writes go to all nodes
	 */
	@SafeVarargs
	public RedundantIndexedSortedMapStorageNode(N... nodes){
		super(List.of(nodes), nodes[0]);
	}

	public RedundantIndexedSortedMapStorageNode(List<N> nodes){
		super(nodes, nodes.get(0));
	}

}
