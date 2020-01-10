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
package io.datarouter.client.hbase.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.util.tuple.Range;

// broken because of DATAROUTER-2622
@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HbaseRangeIntegrationTester{

	@Inject
	private HbaseRangeTestDatabeanDao testDatabeanDao;

	@Test
	public void testRange(){
		testDatabeanDao.deleteAll();
		Collection<TestDatabean> databeans = Arrays.asList(
				new TestDatabean("10", null, null),
				new TestDatabean("11", null, null),
				new TestDatabean("12", null, null),
				new TestDatabean("120", null, null),
				new TestDatabean("121", null, null),
				new TestDatabean("122", null, null),
				new TestDatabean("123", null, null),
				new TestDatabean("13", null, null),
				new TestDatabean("14", null, null),
				new TestDatabean("15", null, null));
		testDatabeanDao.putMulti(databeans);

		List<TestDatabeanKey> list = testDatabeanDao.scanKeys(new Range<>(new TestDatabeanKey("12"), false))
				.list();
		Assert.assertEquals(list.get(0), new TestDatabeanKey("120"));
	}

}