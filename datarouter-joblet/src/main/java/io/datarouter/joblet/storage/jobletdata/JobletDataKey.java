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
package io.datarouter.joblet.storage.jobletdata;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.number.RandomTool;

public class JobletDataKey extends BaseRegularPrimaryKey<JobletDataKey>{

	private Long id;

	public static class FieldKeys{
		public static final UInt63FieldKey id = new UInt63FieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new UInt63Field(FieldKeys.id, id));
	}

	public JobletDataKey(Long id){
		this.id = id;
	}

	public JobletDataKey(){
		this(RandomTool.nextPositiveLong());
	}

	public Long getId(){
		return id;
	}

}