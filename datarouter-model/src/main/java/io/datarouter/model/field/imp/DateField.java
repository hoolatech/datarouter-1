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
package io.datarouter.model.field.imp;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.DateTool;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.string.StringTool;

public class DateField extends BasePrimitiveField<Date,DateFieldKey>{

	public DateField(DateFieldKey key, Date value){
		this(null, key, value);
	}

	public DateField(String prefix, DateFieldKey key, Date value){
		super(prefix, key, value);
	}

	public int getNumDecimalSeconds(){
		return ((DateFieldKey)getKey()).getNumDecimalSeconds();
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return DateTool.getInternetDate(value, getNumDecimalSeconds());
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		Instant instant = DateTimeFormatter.ISO_INSTANT.parse(str, Instant::from);
		long epochMilli = instant.toEpochMilli();
		long divider = (long)Math.pow(10, 3 - getNumDecimalSeconds());
		epochMilli = epochMilli / divider * divider;
		return new Date(epochMilli);
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return LongByteTool.getUInt63Bytes(value.getTime());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}

}
