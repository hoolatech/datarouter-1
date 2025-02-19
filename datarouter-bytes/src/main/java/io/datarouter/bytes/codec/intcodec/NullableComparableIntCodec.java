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
package io.datarouter.bytes.codec.intcodec;

public class NullableComparableIntCodec{

	public static final NullableComparableIntCodec INSTANCE = new NullableComparableIntCodec();

	private static final int NULL = Integer.MIN_VALUE;
	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final int LENGTH = COMPARABLE_INT_CODEC.length();

	public int length(){
		return LENGTH;
	}

	public byte[] encode(Integer value){
		int nonNullValue = value == null ? NULL : value;
		return COMPARABLE_INT_CODEC.encode(nonNullValue);
	}

	public int decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public Integer decode(byte[] bytes, int offset){
		int nonNullValue = COMPARABLE_INT_CODEC.decode(bytes, offset);
		return nonNullValue == NULL ? null : nonNullValue;
	}

}
