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
package io.datarouter.exception.storage.exceptionrecord;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public abstract class BaseExceptionRecordKey<PK extends RegularPrimaryKey<PK>>
extends BaseRegularPrimaryKey<PK>{

	private String id;

	public static class FieldKeys{
		public static final StringFieldKey id = new StringFieldKey("id");
	}

	public BaseExceptionRecordKey(){
	}

	public BaseExceptionRecordKey(String id){
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new StringField(FieldKeys.id, id));
	}

	public String getId(){
		return id;
	}

}
