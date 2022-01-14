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
package io.datarouter.bytes.codec.array.longarray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

public class ComparableLongArrayCodec{

	public static final ComparableLongArrayCodec INSTANCE = new ComparableLongArrayCodec();

	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_LONG_CODEC.length();

	public int itemLength(){
		return ITEM_LENGTH;
	}

	public byte[] encode(long[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		byte[] bytes = new byte[ITEM_LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(long[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_LONG_CODEC.encode(values[i], bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.length * ITEM_LENGTH;
	}

	public long[] decode(byte[] bytes){
		if(bytes == null){ //TODO remove null check
			return EmptyArray.LONG;
		}
		return decode(bytes, 0, bytes.length);
	}

	public long[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.LONG;
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int resultLength = bytesLength / ITEM_LENGTH;
		long[] result = new long[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_LONG_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return result;
	}

}