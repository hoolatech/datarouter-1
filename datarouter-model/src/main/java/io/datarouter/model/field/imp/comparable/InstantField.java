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
package io.datarouter.model.field.imp.comparable;

import java.time.Instant;

import io.datarouter.bytes.LongArray;
import io.datarouter.bytes.codec.array.longarray.UInt63ArrayCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.model.util.FractionalSecondTool;

public class InstantField extends BasePrimitiveField<Instant,InstantFieldKey>{

	private static final UInt63ArrayCodec U_INT_63_ARRAY_CODEC = UInt63ArrayCodec.INSTANCE;

	public InstantField(InstantFieldKey key, Instant value){
		super(key, FractionalSecondTool.truncate(value, key.getNumFractionalSeconds()));
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Instant parseStringEncodedValueButDoNotSet(String value){
		return Instant.parse(value);
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		LongArray longArray = new LongArray(2);
		longArray.add(value.getEpochSecond());
		longArray.add(value.getNano());
		return U_INT_63_ARRAY_CODEC.encode(longArray.getPrimitiveArray());
	}

	@Override
	public Instant fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		long[] secondsAndNanos = U_INT_63_ARRAY_CODEC.decode(bytes, byteOffset, 16);
		return Instant.ofEpochSecond(secondsAndNanos[0], secondsAndNanos[1]);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return 16;
	}

}
