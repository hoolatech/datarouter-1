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
package io.datarouter.storage.test.node.basic.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanKey;

public abstract class BaseMapStorageIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	private MapStorage<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	protected void setup(ClientId clientId, boolean entity){
		mapStorageNode = new DatarouterMapStorageTestRouter(datarouter, nodeFactory, wideNodeFactory, clientId,
				entity).mapStorageNode;
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testGet(){
		List<MapStorageBean> beans = initBeans(1000);
		mapStorageNode.putMulti(beans);
		final int sampleEveryN = 29;
		for(int i = 0; i < beans.size(); i += sampleEveryN){
			MapStorageBean bean = beans.get(i);
			MapStorageBean roundTripped = mapStorageNode.get(bean.getKey());
			Assert.assertEquals(roundTripped, bean);
		}
		deleteRecords(beans);
	}

	@Test
	public void testGetMulti(){
		List<MapStorageBean> beans = initBeans(10);
		mapStorageNode.putMulti(beans);

		List<MapStorageBeanKey> keys = DatabeanTool.getKeys(beans);
		List<MapStorageBean> roundTripped = mapStorageNode.getMulti(keys);
		Assert.assertEquals(roundTripped.size(), beans.size());

		for(MapStorageBean bean : beans){
			Assert.assertTrue(roundTripped.contains(bean));
		}

		deleteRecords(beans);
	}

	@Test
	public void testPutMulti(){
		List<MapStorageBean> beans = initBeans(10);

		for(MapStorageBean bean : beans){
			Assert.assertFalse(mapStorageNode.exists(bean.getKey()));
		}

		mapStorageNode.putMulti(beans);

		for(MapStorageBean bean : beans){
			Assert.assertTrue(mapStorageNode.exists(bean.getKey()));
		}

		deleteRecords(beans);
	}

	@Test
	public void testBlankDatabeanPut(){
		MapStorageBean blankDatabean = new MapStorageBean(null);
		MapStorageBean nonBlankDatabean = new MapStorageBean("a");
		mapStorageNode.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean));
		MapStorageBean roundTrippedBlank = mapStorageNode.get(blankDatabean.getKey());
		new MapStorageBeanFielder().getNonKeyFields(roundTrippedBlank).stream()
				.map(Field::getValue)
				.forEach(Assert::assertNull);
		mapStorageNode.deleteMulti(DatabeanTool.getKeys(Arrays.asList(blankDatabean, nonBlankDatabean)));
		Assert.assertNull(mapStorageNode.get(blankDatabean.getKey()));
	}


	@Test
	public void testGetKeys(){
		MapStorageBean bean0 = new MapStorageBean();
		MapStorageBean bean1 = new MapStorageBean(); // not inserted to db
		MapStorageBean bean2 = new MapStorageBean();

		mapStorageNode.put(bean0);
		mapStorageNode.put(bean2);

		List<MapStorageBeanKey> keysToGet = Arrays.asList(bean0.getKey(), bean1.getKey(), bean2.getKey());
		List<MapStorageBeanKey> keysGotten = mapStorageNode.getKeys(keysToGet);

		Assert.assertTrue(keysGotten.contains(bean0.getKey()));
		Assert.assertFalse(keysGotten.contains(bean1.getKey()));
		Assert.assertTrue(keysGotten.contains(bean2.getKey()));

		deleteRecord(bean0);
		deleteRecord(bean2);
	}

	private List<MapStorageBean> initBeans(int size){
		List<MapStorageBean> beans = new ArrayList<>();
		for(int i = 0; i < size; i++){
			beans.add(new MapStorageBean("data" + i));
		}
		return beans;
	}

	private void deleteRecord(MapStorageBean bean){
		mapStorageNode.delete(bean.getKey());
	}

	private void deleteRecords(List<MapStorageBean> beans){
		for(MapStorageBean bean : beans){
			deleteRecord(bean);
		}
	}

}
