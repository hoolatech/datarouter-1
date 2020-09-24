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
package io.datarouter.model.field.imp.array;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveLongArrayFieldTests{

	@Test
	public void testStringEncodedValue(){
		PrimitiveLongArrayField field;

		long[] array = {1, -12, 15};
		String stringValue = "[1,-12,15]";
		field = new PrimitiveLongArrayField(new PrimitiveLongArrayFieldKey("test"), array);
		Assert.assertEquals(field.getStringEncodedValue(), stringValue);
		Assert.assertEquals(field.parseStringEncodedValueButDoNotSet(stringValue), array);

		long[] emptyArray = {};
		String emptyArrayStringValue = "[]";
		field = new PrimitiveLongArrayField(new PrimitiveLongArrayFieldKey("test"), emptyArray);
		Assert.assertEquals(field.getStringEncodedValue(), emptyArrayStringValue);
		Assert.assertEquals(field.parseStringEncodedValueButDoNotSet(emptyArrayStringValue), emptyArray);

		field = new PrimitiveLongArrayField(new PrimitiveLongArrayFieldKey("test"), null);
		Assert.assertNull(field.getStringEncodedValue());
		Assert.assertNull(field.parseStringEncodedValueButDoNotSet(null));
	}

}
