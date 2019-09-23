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
package io.datarouter.storage.test.node.basic.manyfield;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.array.LongArray;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.collection.ListTool;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public abstract class BaseManyFieldIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected MapStorageNode<ManyFieldBeanKey,ManyFieldBean,ManyFieldTypeBeanFielder> mapNode;

	public void setup(ClientId clientId, Supplier<ManyFieldTypeBeanFielder> fielderSupplier){
		DatarouterManyFieldTestRouter router = new DatarouterManyFieldTestRouter(datarouter, nodeFactory, clientId,
				fielderSupplier);
		mapNode = router.manyFieldTypeBean();

		resetTable();
	}

	private void resetTable(){
		try{
			mapNode.deleteAll();
		}catch(UnsupportedOperationException e){
			// some storage can't do that
		}
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testDelete(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)12);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped, bean);

		mapNode.delete(bean.getKey());
		Assert.assertNull(mapNode.get(bean.getKey()));
	}

	@Test
	public void testBoolean(){
		ManyFieldBean bean = new ManyFieldBean();

		//test true value
		bean.setBooleanField(true);
		mapNode.put(bean);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getBooleanField(), bean.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		mapNode.put(bean);
		ManyFieldBean roundTrippedFalse = mapNode.get(bean.getKey());
		Assert.assertNotSame(roundTrippedFalse, bean);
		Assert.assertEquals(roundTrippedFalse.getBooleanField(), bean.getBooleanField());

	}

	@Test
	public void testByte(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setByteField((byte)-57);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getByteField(), bean.getByteField());
	}

	@Test
	public void testShort(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)-57);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getShortField(), bean.getShortField());
	}

	@Test
	public void testInteger(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(-100057);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(12345);
		mapNode.put(bean);
		roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(-77);
		mapNode.put(bean);
		roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
		Assert.assertEquals(roundTripped.getIntegerField().intValue(), -77);
	}

	@Test
	public void testLong(){
		ManyFieldBean bean = new ManyFieldBean();
		long negative6Billion = 3 * (long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongField(), bean.getLongField());
		Assert.assertTrue(negative6Billion == roundTripped.getLongField());
	}

	@Test
	public void testFloat(){
		ManyFieldBean bean = new ManyFieldBean();
		float val = -157.34f;
		bean.setFloatField(val);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertNotNull(roundTripped);
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertTrue(val == roundTripped.getFloatField());
	}

	@Test
	public void testNullPrimitive(){
		ManyFieldBean bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertEquals(roundTripped.getFloatField(), val);
	}

	@Test
	public void testDouble(){
		ManyFieldBean bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDoubleField(), bean.getDoubleField());
		Assert.assertTrue(val == roundTripped.getDoubleField());
	}

	@Test
	public void testLongDate(){
		ManyFieldBean bean = new ManyFieldBean();
		Date val = new Date();
		bean.setLongDateField(val);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongDateField(), bean.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
	}

	@Test
	public void testLocalDate(){
		ManyFieldBean bean = new ManyFieldBean();
		LocalDate val = LocalDate.now();
		bean.setLocalDateField(val);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLocalDateField(), bean.getLocalDateField());
		Assert.assertTrue(val.equals(roundTripped.getLocalDateField()));
	}

	@Test
	public void testLocalDateTime(){
		ManyFieldBean bean = new ManyFieldBean();
		// LocalDateTime.now() uses the system clock as default so it will always get fractional seconds up to 3 digits
		// (i.e. milliseconds) and no more.
		LocalDateTime val = LocalDateTime.now();
		bean.setDateTimeField(val);
		mapNode.put(bean);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDateTimeField(), bean.getDateTimeField());
		Assert.assertEquals(roundTripped.getDateTimeField(), val);

		// LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		// MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		// so the value of the LocalDateTime retrieved from the database will not be equal to the LocalDateTime saved
		LocalDateTime valOutOfBounds = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 50);
		bean.setDateTimeField(valOutOfBounds);
		mapNode.put(bean);
		ManyFieldBean roundTripped2 = mapNode.get(bean.getKey());
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), bean.getDateTimeField());
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), valOutOfBounds);
		/* LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		to 3 digits so the value of the LocalDateTime retrieved from the database will not be equal to the
		LocalDateTime saved */
		LocalDateTime localDateTimeWithNano = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060750);
		LocalDateTime localDateTimeTruncated = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060000);
		bean.setDateTimeField(localDateTimeWithNano);
		mapNode.put(bean);
		ManyFieldBean roundTripped3 = mapNode.get(bean.getKey());
		Assert.assertNotEquals(localDateTimeWithNano, localDateTimeTruncated);
		Assert.assertEquals(roundTripped3.getDateTimeField(), localDateTimeTruncated);
	}

	@Test
	public void testInstant(){
		Instant instant = Instant.now();
		ManyFieldBean bean = new ManyFieldBean();
		bean.setInstantField(instant);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getInstantField(), bean.getInstantField());
		Assert.assertEquals(roundTripped.getInstantField(), instant);
	}

	@Test
	public void testCharacter(){
		ManyFieldBean bean = new ManyFieldBean();
		Character charQ = 'Q';
		bean.setCharacterField(charQ);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getCharacterField(), bean.getCharacterField());
		Assert.assertEquals(roundTripped.getCharacterField(), charQ);
	}

	@Test
	public void testString(){
		ManyFieldBean bean = new ManyFieldBean();
		String multiByteUtf8Char = "😀";
		String val = "abcdef" + multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StandardCharsets.UTF_8));
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getStringField(), bean.getStringField());
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StandardCharsets.UTF_8);
		Assert.assertEquals(val, roundTrippedByteString);
	}


	@Test
	public void testByteArray(){
		ManyFieldBean bean = new ManyFieldBean();
		byte[] value = new byte[]{1, 5, -128, 127, 25, 66, -80, -12};

		bean.setByteArrayField(value);
		mapNode.put(bean);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getByteArrayField(), value);
	}

	@Test
	public void testVarInt(){

		ManyFieldBean bean0 = new ManyFieldBean();
		bean0.setVarIntField(0);
		mapNode.put(bean0);

		ManyFieldBean roundTripped0 = mapNode.get(bean0.getKey());
		Assert.assertNotSame(roundTripped0, bean0);
		Assert.assertEquals(roundTripped0.getVarIntField(), bean0.getVarIntField());

		//1234567
		ManyFieldBean bean1234567 = new ManyFieldBean();
		bean1234567.setVarIntField(1234567);
		mapNode.put(bean1234567);

		ManyFieldBean roundTripped1234567 = mapNode.get(bean1234567.getKey());
		Assert.assertNotSame(roundTripped1234567, bean1234567);
		Assert.assertEquals(roundTripped1234567.getVarIntField(), bean1234567.getVarIntField());

		//Integer.MAX_VALUE
		ManyFieldBean beanMax = new ManyFieldBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		mapNode.put(beanMax);

		ManyFieldBean roundTrippedMax = mapNode.get(beanMax.getKey());
		Assert.assertNotSame(roundTrippedMax, beanMax);

		Assert.assertEquals(roundTrippedMax.getVarIntField(), beanMax.getVarIntField());
	}

	@Test
	public void testIntegerEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntEnumField(TestEnum.beast);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntEnumField(), bean.getIntEnumField());
		Assert.assertEquals(roundTripped.getIntEnumField(), TestEnum.beast);
	}

	@Test
	public void testVarIntEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setVarIntEnumField(TestEnum.fish);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getVarIntEnumField(), bean.getVarIntEnumField());
		Assert.assertEquals(roundTripped.getVarIntEnumField(), TestEnum.fish);
	}

	@Test
	public void testStringEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setStringEnumField(TestEnum.cat);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getStringEnumField(), bean.getStringEnumField());
		Assert.assertEquals(roundTripped.getStringEnumField(), TestEnum.cat);
	}

	@Test
	public void testBlob(){
		LongArray ids = new LongArray();
		ids.add(5L);
		ids.add(10L);
		ids.add(15L);
		ids.add(126L);
		byte[] bytes = LongByteTool.getComparableByteArray(ids);

		ManyFieldBean bean = new ManyFieldBean();
		bean.setData(bytes);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(LongByteTool.fromComparableByteArray(roundTripped.getData()),
				ArrayTool.primitiveLongArray(ids));
	}

	@Test
	public void testUInt31(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(7888);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
	}

	@Test
	public void testLongArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongArrayField(), bean.getLongArrayField());
	}

	@Test
	public void testBooleanArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getBooleanArrayField(), bean.getBooleanArrayField());
	}

	@Test
	public void testIntegerArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerArrayField(), bean.getIntegerArrayField());
	}

	@Test
	public void testDoubleArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDoubleArrayField(), bean.getDoubleArrayField());
	}

	@Test
	public void testDelimitedStringArray(){
		ManyFieldBean bean = new ManyFieldBean();
		List<String> strings = ListTool.create("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		mapNode.put(bean);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDelimitedStringArrayField().toArray(), strings.toArray());
	}

}
