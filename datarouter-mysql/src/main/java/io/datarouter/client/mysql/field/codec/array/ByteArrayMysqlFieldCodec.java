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
package io.datarouter.client.mysql.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayMysqlFieldCodec
extends BaseMysqlFieldCodec<byte[],ByteArrayField>{

	public ByteArrayMysqlFieldCodec(ByteArrayField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		MysqlColumnType type = getMysqlColumnType();
		int size = type == MysqlColumnType.LONGBLOB ? Integer.MAX_VALUE : field.getKey().getSize();
		return new SqlColumn(field.getKey().getColumnName(), type, size, nullable, false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, field.getValue());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getBytes(field.getKey().getColumnName());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		int size = field.getKey().getSize();
		if(size <= CommonFieldSizes.MAX_LENGTH_VARBINARY){
			return MysqlColumnType.VARBINARY;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_LONGBLOB){
			return MysqlColumnType.LONGBLOB;
		}
		throw new IllegalArgumentException("Size:" + size + " is larger than max supported size: "
				+ CommonFieldSizes.MAX_LENGTH_LONGBLOB + " (CommonFieldSizes.MAX_LENGTH_LONGBLOB)");
	}

}
