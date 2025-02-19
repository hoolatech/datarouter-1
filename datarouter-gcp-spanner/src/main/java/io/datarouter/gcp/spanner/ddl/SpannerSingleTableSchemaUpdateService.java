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
package io.datarouter.gcp.spanner.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;

import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.node.entity.SpannerSubEntityNode;
import io.datarouter.gcp.spanner.util.SpannerEntityKeyTool;
import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.string.StringTool;

@Singleton
public class SpannerSingleTableSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(SpannerSingleTableSchemaUpdateService.class);

	private static final SpannerColumn PARTITION_COLUMN = new SpannerColumn(
			SpannerSubEntityNode.PARTITION_COLUMN_NAME,
			SpannerColumnType.INT64,
			false);

	@Inject
	private SpannerDatabaseClientsHolder clientsHolder;
	@Inject
	private SpannerTableOperationsGenerator tableOperationsGenerator;
	@Inject
	private SpannerFieldCodecRegistry fieldCodecRegistry;
	@Inject
	private SpannerTableAlterSchemaService tableAlterSchemaService;
	@Inject
	private SchemaUpdateOptions updateOptions;

	public Optional<SchemaUpdateResult> performSchemaUpdate(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> physicalNode){
		String tableName;
		List<Field<?>> primaryKeyFields = SpannerEntityKeyTool.getPrimaryKeyFields(
				physicalNode.getFieldInfo().getSamplePrimaryKey(),
				physicalNode.getFieldInfo().isSubEntity());
		if(physicalNode.getFieldInfo().isSubEntity()){
			tableName = SpannerEntityKeyTool.getEntityTableName(physicalNode.getFieldInfo());
		}else{
			tableName = physicalNode.getFieldInfo().getTableName();
		}
		List<? extends SpannerBaseFieldCodec<?,?>> primaryKeyCodecs = fieldCodecRegistry.createCodecs(primaryKeyFields);
		for(SpannerBaseFieldCodec<?,?> codec : primaryKeyCodecs){
			if(codec.getSpannerColumnType().isArray()){
				throw new RuntimeException("Invalid field type used for primary key: " + codec.getField().getKey()
						.getName());
			}
		}
		List<SpannerIndex> indexes = new ArrayList<>();
		List<SpannerIndex> uniqueIndexes = Scanner.of(physicalNode.getFieldInfo().getUniqueIndexes().entrySet())
				.map(entry -> new SpannerIndex(
						tableName,
						entry.getKey(),
						entry.getValue(),
						Collections.emptyList(),
						true))
				.list();
		var statements = new SpannerUpdateStatements();
		String entityTableName = null;
		if(physicalNode instanceof IndexedStorage){
			IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
			indexes = Scanner.of(indexedStorage.getManagedNodes())
					.map(node -> new SpannerIndex(
							tableName,
							node.getName(),
							node.getIndexEntryFieldInfo().getPrimaryKeyFields(),
							node.getIndexEntryFieldInfo().getFields(),
							false))
					.list();
		}
		List<SpannerColumn> primaryKeyColumns = Scanner.of(primaryKeyCodecs)
				.map(codec -> codec.getSpannerColumn(false))
				.list();
		List<SpannerColumn> nonKeyColumns = Scanner.of(fieldCodecRegistry.createCodecs(
				physicalNode.getFieldInfo().getNonKeyFields()))
				.map(codec -> codec.getSpannerColumn(true))
				.list();
		if(physicalNode.getFieldInfo().isSubEntity()){
			primaryKeyColumns.add(0, PARTITION_COLUMN);
			entityTableName = physicalNode.getFieldInfo().getTableName();
			if(!existingTableNames.get().contains(entityTableName)){
				List<? extends SpannerBaseFieldCodec<?,?>> entityKeyCodecs = fieldCodecRegistry.createCodecs(
						physicalNode.getFieldInfo().getEkPkFields());
				List<SpannerColumn> entityColumns = Scanner.of(entityKeyCodecs)
						.map(codec -> codec.getSpannerColumn(false))
						.list();
				entityColumns.add(0, PARTITION_COLUMN);
				statements.updateFunction(
						tableOperationsGenerator.createTable(entityTableName, entityColumns, null, null),
						updateOptions::getCreateTables,
						true);
			}
		}
		if(!existingTableNames.get().contains(tableName)){
			statements.updateFunction(
					tableOperationsGenerator.createTable(tableName, primaryKeyColumns, nonKeyColumns, entityTableName),
					updateOptions::getCreateTables,
					true);
			Scanner.of(indexes, uniqueIndexes)
					.concat(Scanner::of)
					.map(index -> createIndex(index, primaryKeyColumns))
					.forEach(statement -> statements.updateFunction(
							statement,
							updateOptions::getCreateTables,
							true));
		}else{
			DatabaseClient databaseClient = clientsHolder.getDatabaseClient(clientId);
			List<SpannerColumn> allColumns = Scanner.of(primaryKeyColumns, nonKeyColumns)
					.concat(Scanner::of)
					.list();
			if(physicalNode.getFieldInfo().isSubEntity()){
				allColumns.add(0, PARTITION_COLUMN);
			}
			ResultSet columnRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
					.getTableSchema(tableName)));
			ResultSet primaryKeyRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
					.getTableIndexColumnsSchema(tableName, "PRIMARY_KEY")));
			tableAlterSchemaService.generateUpdateStatementColumns(tableName, allColumns, primaryKeyColumns,
					columnRs, primaryKeyRs, statements);
			ResultSet indexesRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
					.getTableIndexSchema(tableName)));
			Set<String> currentIndexes = tableAlterSchemaService.getIndexes(indexesRs);

			Scanner.of(indexes, uniqueIndexes)
					.concat(Scanner::of)
					.forEach(index -> {
						Statement tableIndexColumnsSchema = Statement.of(tableOperationsGenerator
								.getTableIndexColumnsSchema(tableName, index.getIndexName()));
						ResultSet indexRs = databaseClient.singleUse().executeQuery(tableIndexColumnsSchema);
						if(!tableAlterSchemaService.indexEqual(index, indexRs)){
							if(currentIndexes.contains(index.getIndexName())){
								statements.updateFunction(
										tableOperationsGenerator.dropIndex(index.getIndexName()),
										updateOptions::getDropIndexes,
										false);
							}
							statements.updateFunction(
									createIndex(index, primaryKeyColumns),
									updateOptions::getAddIndexes,
									true);
						}
						currentIndexes.remove(index.getIndexName());
					});

			currentIndexes.forEach(name -> statements.updateFunction(
					tableOperationsGenerator.dropIndex(name),
					updateOptions::getDropIndexes,
					false));
		}
		String errorMessage = null;
		if(!statements.getExecuteStatements().isEmpty()){
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Executing Spanner " + getClass().getSimpleName()
					+ " SchemaUpdate"));
			logger.info(String.join("\n\n", statements.getExecuteStatements()));
			Database database = clientsHolder.getDatabase(clientId);
			OperationFuture<Void,UpdateDatabaseDdlMetadata> future = database.updateDdl(
					statements.getExecuteStatements(), null);
			errorMessage = FutureTool.get(future.getPollingFuture().getAttemptResult()).getErrorMessage();
			if(StringTool.notNullNorEmptyNorWhitespace(errorMessage)){
				logger.error(errorMessage);
			}
		}
		if(statements.getPreventStartUp()){
			errorMessage = "an alter on Spanner table " + tableName + " is required";
		}
		if(statements.getPrintStatements().isEmpty()){
			return Optional.empty();
		}
		String printStatement = statements.getPrintStatements().stream()
				.map(statement -> statement + ";")
				.collect(Collectors.joining("\n"));

		logger.info(SchemaUpdateTool.generateFullWidthMessage("Please Execute Spanner SchemaUpdate"));
		logger.info(printStatement);
		logger.info(SchemaUpdateTool.generateFullWidthMessage("Thank You"));
		return Optional.of(new SchemaUpdateResult(printStatement, errorMessage, clientId));
	}

	private String createIndex(SpannerIndex index, List<SpannerColumn> primaryKeyColumns){
		List<SpannerColumn> keyColumns = Scanner.of(fieldCodecRegistry.createCodecs(index.getKeyFields()))
				.map(codec -> codec.getSpannerColumn(false))
				.list();
		if(index.getNonKeyFields().isEmpty()){
			return tableOperationsGenerator.createIndex(index.getTableName(), index.getIndexName(), keyColumns,
					Collections.emptyList(), index.isUnique());
		}
		// Spanner stores the primary key columns in the index automatically and will not create the index if
		// told to explicitly store a primary key column
		Set<String> primaryKeySet = Scanner.of(primaryKeyColumns)
				.map(SpannerColumn::getName)
				.collect(HashSet::new);
		List<SpannerColumn> nonKeyColumns = Scanner.of(fieldCodecRegistry.createCodecs(index.getNonKeyFields()))
				.map(codec -> codec.getSpannerColumn(false))
				.exclude(col -> primaryKeySet.contains(col.getName()))
				.list();
		return tableOperationsGenerator.createIndex(
				index.getTableName(),
				index.getIndexName(),
				keyColumns,
				nonKeyColumns,
				index.isUnique());
	}

}
