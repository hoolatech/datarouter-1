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
package io.datarouter.trace.storage.span;

import java.util.List;

import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.trace.storage.trace.Trace2;

public class Trace2Span
extends BaseDatabean<Trace2SpanKey,Trace2Span>{

	private Integer parentSequence;
	private String name;
	private Long created;
	private Long ended;
	private String info;

	public static class FieldKeys{
		public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
		public static final StringFieldKey name = new StringFieldKey("name")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
	}

	public static class Trace2SpanFielder extends BaseDatabeanFielder<Trace2SpanKey,Trace2Span>{

		public Trace2SpanFielder(){
			super(Trace2SpanKey.class);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Span databean){
			return List.of(
					new UInt31Field(FieldKeys.parentSequence, databean.parentSequence),
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.info, databean.info),
					new UInt63Field(FieldKeys.created, databean.created),
					new UInt63Field(FieldKeys.ended, databean.ended));
		}
	}

	public Trace2Span(){
		this(new Trace2SpanKey());
	}

	public Trace2Span(Trace2SpanKey key){
		super(key);
	}

	public Trace2Span(Trace2SpanDto dto){
		super(new Trace2SpanKey(dto.traceparent, dto.parentThreadId, dto.sequence));
		this.parentSequence = dto.parentSequence;
		this.name = dto.name;
		this.created = dto.created;
		this.ended = dto.ended;
		this.info = dto.info;
	}

	@Override
	public Class<Trace2SpanKey> getKeyClass(){
		return Trace2SpanKey.class;
	}

	public boolean isTopLevel(){
		return this.parentSequence == null;
	}

	public Long getThreadId(){
		return getKey().getThreadId();
	}

	public Integer getSequence(){
		return getKey().getSequence();
	}

	public String getName(){
		return name;
	}

	public Long getCreated(){
		return created;
	}

	public Long getEnded(){
		return ended;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public String getInfo(){
		return info;
	}

	@Override
	public String toString(){
		return getKey() + "[" + name + "][" + info + "]";
	}

}
