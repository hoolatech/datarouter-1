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
package io.datarouter.storage.node.op.raw.read;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.op.util.SortedStorageCountingTool;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.iterable.scanner.Scanner;
import io.datarouter.util.tuple.Range;

/**
 * Methods for reading from storage mechanisms that keep databeans sorted by PrimaryKey.  Similar to java's TreeMap.
 *
 * Possible implementations include TreeMap, RDBMS, HBase, LevelDB, Google Cloud Datastore, Google Cloud BigTable, etc
 */
public interface SortedStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String OP_getKeysInRange = "getKeysInRange";
	public static final String OP_getRange = "getRange";
	public static final String OP_getPrefixedRange = "getPrefixedRange";
	public static final String OP_scanKeys = "scanKeys";
	public static final String OP_scanKeysMulti = "scanKeysMulti";
	public static final String OP_scan = "scan";
	public static final String OP_scanMulti = "scanMulti";

	/*-------------------------------- scan multi ---------------------------------*/

	Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config);

	default Stream<D> streamMulti(Collection<Range<PK>> ranges, Config config){
		return scanMulti(ranges, config).stream();
	}

	/*-------------------------------- scan ---------------------------------*/

	/**
	 * The scan method accepts a Range&lt;PK&gt; which identifies the startKey and endKey, and returns all contiguous
	 * rows between them, not skipping or filtering any. Implementations will generally query the database in batches to
	 * avoid long transactions and huge result sets. <br>
	 * <br>
	 * When providing startKey and endKey, implementations will ignore fields after the first null.  For example, when
	 * scanning a phone book with startKey: <br>
	 * * (null, null) is valid and will start at the beginning of the book<br>
	 * * (Corgan, null) is valid and will start at the first Corgan <br>
	 * * (Corgan, Matt) is valid and will start at Corgan, Matt <br>
	 * * (null, Matt) is invalid.  The Matt is ignored, so it is equivalent to (null, null) <br>
	 * <br>
	 * Note that (null, Matt) will NOT do any filtering for rows with firstName=Matt. To avoid tablescans we are
	 * returning all rows in the range to the client where the client can then filter. A predicate push-down feature may
	 * be added, but it will likely use a separate interface method.
	 */
	default Scanner<D> scan(final Range<PK> range, final Config config){
		return scanMulti(Arrays.asList(Range.nullSafe(range)), config);
	}

	default Stream<D> stream(Range<PK> range, Config config){
		return scan(range, config).stream();
	}

	/*-------------------------------- scan keys multi ------------------------------*/

	Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config);

	default Stream<PK> streamKeysMulti(Collection<Range<PK>> ranges, Config config){
		return scanKeysMulti(ranges, config).stream();
	}

	/*-------------------------------- scan keys ------------------------------*/

	default Scanner<PK> scanKeys(final Range<PK> range, final Config config){
		return scanKeysMulti(Arrays.asList(Range.nullSafe(range)), config);
	}

	default Stream<PK> streamKeys(Range<PK> range, Config config){
		return scanKeys(range, config).stream();
	}

	/*-------------------------------- prefix -------------------------------*/

	default Iterable<D> scanWithPrefix(PK prefix, Config config){
		return scan(KeyRangeTool.forPrefix(prefix), config);
	}

	default Stream<D> streamWithPrefix(PK prefix, Config config){
		return scan(KeyRangeTool.forPrefix(prefix), config).stream();
	}

	/*-------------------------------- prefix keys -------------------------------*/

	default Scanner<PK> scanKeysWithPrefix(PK prefix, Config config){
		return scanKeys(KeyRangeTool.forPrefix(prefix), config);
	}

	default Stream<PK> streamKeysWithPrefix(PK prefix, Config config){
		return scanKeys(KeyRangeTool.forPrefix(prefix), config).stream();
	}

	/*-------------------------------- prefixes -------------------------------*/

	default Scanner<D> scanWithPrefixes(Collection<PK> prefixes, Config config){
		return scanMulti(getRangesFromPrefixes(prefixes), config);
	}

	default Stream<D> streamWithPrefixes(Collection<PK> prefixes, Config config){
		return scanWithPrefixes(prefixes, config).stream();
	}

	/*-------------------------------- prefixes keys -------------------------------*/

	default Scanner<PK> scanKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return scanKeysMulti(getRangesFromPrefixes(prefixes), config);
	}

	default Stream<PK> streamKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return scanKeysMulti(getRangesFromPrefixes(prefixes), config).stream();
	}

	/*-------------------------------- count --------------------------------*/

	default long count(Range<PK> range){
		return SortedStorageCountingTool.count(this, range);
	}

	/*---------------------------- static methods ---------------------------*/

	static <PK extends PrimaryKey<PK>> List<Range<PK>> getRangesFromPrefixes(Collection<PK> prefixes){
		return prefixes.stream()
				.map(KeyRangeTool::forPrefix)
				.collect(Collectors.toList());
	}

	/*---------------------------- sub-interfaces ---------------------------*/

	public interface SortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, SortedStorageReader<PK,D>{
	}


	public interface PhysicalSortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, SortedStorageReaderNode<PK,D,F>{
	}
}
