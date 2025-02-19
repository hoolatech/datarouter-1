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
package io.datarouter.gcp.spanner.field.array;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.model.field.imp.array.DoubleArrayField;

public class SpannerDoubleArrayFieldCodec extends SpannerBaseFieldCodec<List<Double>,DoubleArrayField>{

	public SpannerDoubleArrayFieldCodec(DoubleArrayField field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.FLOAT64_ARRAY;
	}

	@Override
	public Value getSpannerValue(){
		return Value.float64Array(field.getValue());
	}

	@Override
	public Builder setKey(Builder key){
		throw new RuntimeException("Invalid Key type: " + field.getKey().getName());
	}

	@Override
	public List<Double> getValueFromResultSet(ResultSet rs){
		// copy to mutable list
		return new ArrayList<>(rs.getDoubleList(field.getKey().getColumnName()));
	}

}
