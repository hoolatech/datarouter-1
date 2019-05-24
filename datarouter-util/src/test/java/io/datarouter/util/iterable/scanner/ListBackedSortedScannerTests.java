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
package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.imp.ListBackedSortedScanner;

public class ListBackedSortedScannerTests{

	private final ArrayList<Integer> originalList = ListTool.createArrayList(1, 3, 8);
	private final int advanceBy = 2;

	@Test
	public void testFullScan(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		Assert.assertTrue(scanner.advance());
		Assert.assertEquals(scanner.getCurrent().intValue(), 1);
		Assert.assertTrue(scanner.advance());
		Assert.assertEquals(scanner.getCurrent().intValue(), 3);
		Assert.assertTrue(scanner.advance());
		Assert.assertEquals(scanner.getCurrent().intValue(), 8);
		Assert.assertFalse(scanner.advance());
	}

	@Test
	public void testFullIteration(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		Iterator<Integer> iterator = scanner.iterator();
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next().intValue(), 1);
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next().intValue(), 3);
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next().intValue(), 8);
		Assert.assertFalse(iterator.hasNext());
	}

	@Test
	public void testFullIterationWithTool(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		List<Integer> result = scanner.list();
		Assert.assertEquals(result, originalList);
	}

	@Test
	public void testSkipScan(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		scanner.skip(advanceBy);
		Assert.assertTrue(scanner.advance());
		Assert.assertEquals(scanner.getCurrent().intValue(), 8);
		Assert.assertFalse(scanner.advance());
	}

	@Test
	public void testSkipIteration(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		scanner.skip(advanceBy);
		Iterator<Integer> iterator = scanner.iterator();
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next().intValue(), 8);
		Assert.assertFalse(iterator.hasNext());
	}

	@Test
	public void testSkipIterationAfter(){
		ListBackedSortedScanner<Integer> scanner = new ListBackedSortedScanner<>(originalList);
		Iterator<Integer> iterator = scanner.iterator();
		scanner.skip(advanceBy);
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next().intValue(), 8);
		Assert.assertFalse(iterator.hasNext());
	}

}
