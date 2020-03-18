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
package io.datarouter.client.mysql.op.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.exceptions.MysqlErrorNumbers;

import io.datarouter.client.mysql.exception.DuplicateEntrySqlException;
import io.datarouter.client.mysql.node.MysqlNodeManager;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.serialize.fieldcache.FieldGeneratorTool;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;

public class MysqlPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Void>{
	private static final Logger logger = LoggerFactory.getLogger(MysqlPutOp.class);

	private static final int BATCH_SIZE = 100;

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlNodeManager mysqlNodeManager;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final Collection<D> databeans;
	private final Config config;

	public MysqlPutOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			MysqlNodeManager mysqlNodeManager,
			MysqlSqlFactory mysqlSqlFactory,
			Collection<D> databeans,
			Config config){
		super(datarouter, fieldInfo.getClientId(), getIsolation(config), shouldAutoCommit(databeans, config));
		this.fieldInfo = fieldInfo;
		this.mysqlNodeManager = mysqlNodeManager;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.databeans = CollectionTool.nullSafe(databeans);
		this.config = config;
	}

	@Override
	public Void runOnce(){
		Connection connection = getConnection();
		int batchSize = config.optInputBatchSize().orElse(BATCH_SIZE);
		if(PutMethod.INSERT_ON_DUPLICATE_UPDATE == config.getPutMethod()
				&& !fieldInfo.getAutoGeneratedType().isGenerated()){
			Scanner.of(databeans)
					.batch(batchSize)
					.forEach(databeanBatch -> mysqlInsertOnDuplicateKeyUpdate(connection, databeanBatch));
		}else{
			databeans.forEach(databean -> mysqlPutUsingMethod(connection, databean, config.getPutMethod()));
		}
		return null;
	}

	/*-------------------------------private --------------------------------*/

	private static Isolation getIsolation(Config config){
		if(config == null){
			return Isolation.DEFAULT;
		}
		return config.getOption(Isolation.KEY).orElse(Isolation.DEFAULT);
	}

	private static boolean shouldAutoCommit(Collection<? extends Databean<?,?>> databeans, Config config){
		if(CollectionTool.size(databeans) > 1){
			return false;
		}
		return config.getPutMethod().getShouldAutoCommit();
	}

	private void mysqlPutUsingMethod(Connection connection, D databean, PutMethod putMethod){
		Optional<Field<?>> generatedField = FieldGeneratorTool.optFieldToGenerate(fieldInfo, databean);
		if(generatedField.isPresent()){
			if(fieldInfo.getAutoGeneratedType() == FieldGeneratorType.RANDOM){
				FieldGeneratorTool.generateAndSetValueForField(
						fieldInfo,
						databean,
						generatedField.get(),
						bean -> tryMysqlInsertGeneratedId(connection, bean));
			}else{ // The RDBMS will assign the id.
				mysqlInsert(connection, databean);
			}
		}else if(PutMethod.INSERT_OR_BUST == putMethod){
			mysqlInsert(connection, databean);
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			mysqlUpdate(connection, databean, false);
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			try{
				mysqlInsert(connection, databean);
			}catch(RuntimeException e){
				//TODO this will not work inside a txn if not all of the rows already exist
				mysqlUpdate(connection, databean, false);
			}
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			try{
				mysqlUpdate(connection, databean, false);
			}catch(RuntimeException e){
				//TODO this will not work inside a txn if some of the rows already exist
				mysqlInsert(connection, databean);
			}
		}else if(PutMethod.MERGE == putMethod){
			//not really a mysql concept, but usually an update (?)
			try{
				mysqlUpdate(connection, databean, false);
			}catch(RuntimeException e){
				mysqlInsert(connection, databean);
			}
		}else if(PutMethod.INSERT_IGNORE == putMethod){
			mysqlInsert(connection, databean, true);
		}else if(PutMethod.UPDATE_IGNORE == putMethod){
			mysqlUpdate(connection, databean, true);
		}else{
			boolean alreadyExists = mysqlNodeManager.exists(fieldInfo, databean.getKey(), new Config());
			if(alreadyExists){//select before update like hibernate's saveOrUpdate
				mysqlUpdate(connection, databean, false);
			}else{
				mysqlInsert(connection, databean);
			}
		}
	}

	private boolean tryMysqlInsertGeneratedId(Connection connection, D databean){
		try{
			mysqlInsert(connection, databean);
			return true;
		}catch(DuplicateEntrySqlException e){
			return false;
		}
	}

	private void mysqlInsert(Connection connection, D databean){
		mysqlInsert(connection, databean, false);
	}

	private void mysqlInsert(Connection connection, D databean, boolean ignore){
		PreparedStatement preparedStatement = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName())
				.insert(
						fieldInfo.getTableName(),
						Collections.singletonList(fieldInfo.getFieldsWithValues(databean)), ignore)
				.prepareMysql(sql -> connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
		try{
			String spanName = fieldInfo.getNodeName() + " mysqlInsert PreparedStatement.execute";
			try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), spanName)){
				preparedStatement.execute();
			}
			// set autogenerated id
			if(fieldInfo.isManagedAutoGeneratedId()){
				//Retrieve the generated id from DB
				ResultSet resultSet = preparedStatement.getGeneratedKeys();
				resultSet.next();
				Long id = resultSet.getLong(1);
				//set the id
				FieldGeneratorTool.setAutoGeneratedId(fieldInfo, databean, id);
			}
		}catch(Exception e){
			if(e instanceof SQLException && ((SQLException) e).getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
				throw new DuplicateEntrySqlException("error inserting table=" + fieldInfo.getTableName(), e);
			}
			throw new DataAccessException("error inserting table=" + fieldInfo.getTableName() + " sql="
					+ preparedStatement, e);
		}
	}

	private void mysqlInsertOnDuplicateKeyUpdate(Connection connection, Collection<D> databeans){
		List<List<Field<?>>> databeansFields = databeans.stream()
				.map(fieldInfo::getFieldsWithValues)
				.collect(Collectors.toList());
		var sql = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName())
				.insert(fieldInfo.getTableName(), databeansFields, false)
				.append(" on duplicate key update ");
		for(Iterator<Field<?>> iterator = fieldInfo.getFields().iterator(); iterator.hasNext();){
			Field<?> field = iterator.next();
			sql.append(field.getKey().getColumnName())
					.append("=VALUES(")
					.append(field.getKey().getColumnName())
					.append(")");
			if(iterator.hasNext()){
				sql.append(",");
			}
		}
		PreparedStatement preparedStatement = sql.prepare(connection);
		String spanName = fieldInfo.getNodeName() + " mysqlInsertOnDuplicateKeyUpdate PreparedStatement.execute";
		try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), spanName)){
			preparedStatement.execute();
		}catch(Exception e){
			if(e instanceof SQLException && ((SQLException) e).getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
				throw new DuplicateEntrySqlException("error inserting table=" + fieldInfo.getTableName(), e);
			}
			throw new DataAccessException("error inserting table=" + fieldInfo.getTableName() + " rowCount="
					+ databeans.size() + " sql=" + preparedStatement, e);
		}
	}

	private void mysqlUpdate(Connection connection, D databean, boolean ignore){
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFieldsWithValues(databean);
		if(nonKeyFields.isEmpty()){
			logger.warn("Tried to update a databean without non key fields {}", databean, new Exception());
			return;
		}
		String tableName = fieldInfo.getTableName();
		PreparedStatement statement = mysqlSqlFactory
				.createSql(getClientId(), tableName)
				.update(
						tableName,
						nonKeyFields,
						Collections.singletonList(databean.getKey()))
				.prepare(connection);
		int numUpdated;
		String spanName = fieldInfo.getNodeName() + " mysqlUpdate PreparedStatement.execute";
		try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), spanName)){
			numUpdated = statement.executeUpdate();
		}catch(SQLException e){
			throw new DataAccessException("error updating table=" + fieldInfo.getTableName() + " sql=" + statement, e);
		}
		if(!ignore){
			if(numUpdated != 1){
				throw new DataAccessException("error updating table=" + fieldInfo.getTableName() + " row=" + databean
						.getKey().toString() + " not found so could not be updated");
			}
		}
	}
}
