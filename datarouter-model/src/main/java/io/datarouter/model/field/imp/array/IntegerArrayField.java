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
package io.datarouter.model.field.imp.array;

import java.util.List;

import io.datarouter.bytes.codec.list.intlist.IntListCodec;
import io.datarouter.model.field.BaseListField;
import io.datarouter.util.serialization.GsonTool;

public class IntegerArrayField extends BaseListField<Integer,List<Integer>,IntegerArrayFieldKey>{

	private static final IntListCodec INT_LIST_CODEC = IntListCodec.INSTANCE;

	public IntegerArrayField(IntegerArrayFieldKey key, List<Integer> value){
		super(key, value);
	}

	@Override
	public List<Integer> parseStringEncodedValueButDoNotSet(String value){
		return GsonTool.GSON.fromJson(value, getKey().getValueType());
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : INT_LIST_CODEC.encode(value);
	}

	@Override
	public List<Integer> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return INT_LIST_CODEC.decode(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();//why isn't this implemented?
	}

}
