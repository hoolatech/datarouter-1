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
package io.datarouter.gcp.spanner;

import java.util.List;
import java.util.Properties;

import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.gcp.spanner.test.SpannerTestCliendIds;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsBuilder;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao.DatarouterClusterSchemaUpdateLockDaoParams;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;

public class SpannerTestNgModuleFactory extends TestNgModuleFactory{

	public SpannerTestNgModuleFactory(){
		super(List.of(
				new DatarouterWebGuiceModule(),
				new SpannerTestGuiceModule()));
	}

	public static class SpannerTestGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bindActualInstance(DatarouterTestPropertiesFile.class,
					new DatarouterTestPropertiesFile("spanner.properties"));
			bindActual(SchemaUpdateOptionsFactory.class, SpannerSchemaUpdateOptionsFactory.class);
			bind(DatarouterClusterSchemaUpdateLockDaoParams.class)
					.toInstance(new DatarouterClusterSchemaUpdateLockDaoParams(
							List.of(SpannerTestCliendIds.SPANNER)));
			bind(SchemaUpdatesEmailType.class).toInstance(new SchemaUpdatesEmailType(List.of()));
		}

	}

	public static class SpannerSchemaUpdateOptionsFactory implements SchemaUpdateOptionsFactory{

		@Override
		public Properties getInternalConfigDirectoryTypeSchemaUpdateOptions(String internalConfigDirectoryTypeName){
			return new SchemaUpdateOptionsBuilder(true)
					.enableAllSchemaUpdateExecuteOptions()
					.build();
		}

	}

}
