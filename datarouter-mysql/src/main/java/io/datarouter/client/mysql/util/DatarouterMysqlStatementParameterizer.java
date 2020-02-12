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
package io.datarouter.client.mysql.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DatarouterMysqlStatementParameterizer{

	void parameterize(PreparedStatement statement, int index);

	static DatarouterMysqlStatementParameterizer forInt(int value){
		return (ps, index) -> {
			try{
				ps.setInt(index, value);
			}catch(SQLException e){
				throw new RuntimeException(e);
			}
		};
	}

}
