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

import com.google.gson.reflect.TypeToken;

import io.datarouter.model.field.ListFieldKey;

public class DelimitedStringArrayFieldKey extends ListFieldKey<String,List<String>,DelimitedStringArrayFieldKey>{

	public final String separator;

	public DelimitedStringArrayFieldKey(String name){
		this(name, ",");
	}

	public DelimitedStringArrayFieldKey(String name, String separator){
		super(name, new TypeToken<List<String>>(){});
		this.separator = separator;
	}

	@Override
	public DelimitedStringArrayField createValueField(List<String> value){
		return new DelimitedStringArrayField(this, value);
	}

	//hmm, why doesn't this override isFixedLength => false
}
