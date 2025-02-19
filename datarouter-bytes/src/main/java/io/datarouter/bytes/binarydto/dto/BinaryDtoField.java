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
package io.datarouter.bytes.binarydto.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BinaryDtoField{

	public static final int DEFAULT_INDEX = Integer.MIN_VALUE;
	public static final boolean DEFAULT_NULLABLE = true;
	public static final boolean DEFAULT_NULLABLE_ITEMS = true;

	int index() default DEFAULT_INDEX;
	boolean nullable() default DEFAULT_NULLABLE;
	boolean nullableItems() default DEFAULT_NULLABLE_ITEMS;
	Class<? extends BinaryDtoBaseFieldCodec<?>> codec() default BinaryDtoInvalidCodec.class;


	/**
	 * Internal class to represent the lack of a specified codec.
	 */
	public class BinaryDtoInvalidCodec extends BinaryDtoBaseFieldCodec<Object>{

		@Override
		public byte[] encode(Object value){
			throw new RuntimeException("This codec isn't meant to be used.");
		}

		@Override
		public LengthAndValue<Object> decodeWithLength(byte[] bytes, int offset){
			throw new RuntimeException("This codec isn't meant to be used.");
		}

	}
}
