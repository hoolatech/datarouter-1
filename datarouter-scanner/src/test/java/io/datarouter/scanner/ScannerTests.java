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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScannerTests{

	@Test
	public void testScannerOf(){
		Assert.assertSame(Scanner.of().getClass(), EmptyScanner.class);
		Assert.assertSame(Scanner.of(Java9.listOf()).getClass(), EmptyScanner.class); // via RandomAccessScanner

		Assert.assertSame(Scanner.of(1).getClass(), ObjectScanner.class);

		Assert.assertSame(Scanner.of(1, 2).getClass(), ArrayScanner.class);

		Assert.assertSame(Scanner.of(Java9.listOf(1)).getClass(), RandomAccessScanner.class);
		// Collections.unmodifiableList returns UnmodifiableRandomAccessList if the input is RandomAccess
		Assert.assertSame(Scanner.of(Collections.unmodifiableList(Java9.listOf(1))).getClass(),
				RandomAccessScanner.class);
		Assert.assertSame(Scanner.of(Java9.listOf(1)).getClass(), RandomAccessScanner.class);
		Assert.assertSame(Scanner.of(new ArrayList<>(Java9.listOf(1))).getClass(), RandomAccessScanner.class);
		Assert.assertSame(Scanner.of(new Vector<>(Java9.listOf(1))).getClass(), RandomAccessScanner.class);

		Assert.assertSame(Scanner.of(new LinkedList<>(Java9.listOf(1))).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(new HashSet<>(Java9.listOf(1))).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(new TreeSet<>(Java9.listOf(1))).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(Java9.listOf(1).iterator()).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(Stream.of(1).iterator()).getClass(), IteratorScanner.class);

		Assert.assertSame(Scanner.of(Stream.of(1)).getClass(), StreamScanner.class);
	}

	@Test
	public void testConcatIterables(){
		List<Integer> iter1 = Java9.listOf(1);
		List<Integer> iter2 = Java9.listOf();
		List<Integer> iter3 = Java9.listOf(1, 2);
		List<Integer> actual = Scanner.concat(iter1, iter2, iter3).list();
		Assert.assertEquals(actual, Java9.listOf(1, 1, 2));
	}

	@Test
	public void testAppendScanner(){
		List<Integer> actual = Scanner.of(1).append(Scanner.of(Java9.listOf(1, 2))).list();
		Assert.assertEquals(actual, Java9.listOf(1, 1, 2));
	}

	@Test
	public void testAppendVarargs(){
		List<Integer> actual = Scanner.of(1).append(1, 2).list();
		Assert.assertEquals(actual, Java9.listOf(1, 1, 2));
	}

	@Test
	public void testAppendIterable(){
		List<Integer> actual = Scanner.of(1).append(Java9.listOf(1, 2)).list();
		Assert.assertEquals(actual, Java9.listOf(1, 1, 2));
	}

}
