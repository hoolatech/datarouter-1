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
package io.datarouter.bytes.binarydto.fieldcodec.primitive;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.intcodec.VarIntCodec;

public class VarIntBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<Integer>{

	private static final VarIntCodec CODEC = VarIntCodec.INSTANCE;

	@Override
	public byte[] encode(Integer value){
		return CODEC.encode(value);
	}

	@Override
	public Integer decode(byte[] bytes, int offset){
		return CODEC.decode(bytes, offset);
	}

	@Override
	public LengthAndValue<Integer> decodeWithLength(byte[] bytes, int offset){
		int value = decode(bytes, offset);
		int length = CODEC.length(value);
		return new LengthAndValue<>(length, value);
	}

	@Override
	public int compareAsIfEncoded(Integer left, Integer right){
		return Integer.compare(left, right);
	}

}
