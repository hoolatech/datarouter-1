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
package io.datarouter.scanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Java9{

	/**
	 * When switching to Java 9, switch this constant to true and inline it.
	 */
	public static final boolean IS_JAVA_9 = false;

	@SafeVarargs
	public static <T> List<T> listOf(T... items){
		// return List.of(items);

		for(int i = 0; i < items.length; ++i){
			Objects.requireNonNull(items[i]);
		}
		return Collections.unmodifiableList(Arrays.asList(items));
	}

	public static class Entry<K,V>{

		public final K key;
		public final V value;

		public Entry(K key, V value){
			this.key = key;
			this.value = value;
		}

	}

	@SafeVarargs
	public static <K,V> Map<K,V> mapOf(Entry<K,V>...entries){
		// return Map.of(entries);
		Map<K,V> map = new HashMap<>();
		for(int i = 0; i < entries.length; ++i){
			Objects.requireNonNull(entries[i]);
			map.put(entries[i].key, entries[i].value);
		}
		return Collections.unmodifiableMap(map);
	}

}
