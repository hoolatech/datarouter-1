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
package io.datarouter.webappinstance.storage.onetimelogintoken;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class OneTimeLoginTokenKey extends BaseRegularPrimaryKey<OneTimeLoginTokenKey>{

	private final Long userId;

	public OneTimeLoginTokenKey(){
		this(null);
	}

	public OneTimeLoginTokenKey(Long userId){
		this.userId = userId;
	}

	public static class FieldKeys{
		public static final LongFieldKey userId = new LongFieldKey("userId");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new LongField(FieldKeys.userId, userId));
	}

	public Long getUserId(){
		return userId;
	}

}
