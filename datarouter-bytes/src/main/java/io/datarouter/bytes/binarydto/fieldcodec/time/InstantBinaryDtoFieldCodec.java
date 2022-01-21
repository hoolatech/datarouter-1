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
package io.datarouter.bytes.binarydto.fieldcodec.time;

import java.time.Instant;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.time.ComparableInstantCodec;

public class InstantBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<Instant>{

	private static final ComparableInstantCodec CODEC = ComparableInstantCodec.INSTANCE;
	private static final int LENGTH = CODEC.length();

	@Override
	public boolean isFixedLength(){
		return true;
	}

	@Override
	public int fixedLength(){
		return LENGTH;
	}

	@Override
	public byte[] encode(Instant value){
		return CODEC.encode(value);
	}

	@Override
	public Instant decode(byte[] bytes, int offset){
		return CODEC.decode(bytes, offset);
	}

	@Override
	public LengthAndValue<Instant> decodeWithLength(byte[] bytes, int offset){
		int length = fixedLength();
		Instant value = decode(bytes, offset);
		return new LengthAndValue<>(length, value);
	}

	@Override
	public int compareAsIfEncoded(Instant left, Instant right){
		return left.compareTo(right);
	}

}
