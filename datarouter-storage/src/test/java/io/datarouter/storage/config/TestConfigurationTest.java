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
package io.datarouter.storage.config;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.storage.config.properties.InternalConfigDirectory;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public class TestConfigurationTest{
	private static final Logger logger = LoggerFactory.getLogger(TestConfigurationTest.class);

	@Inject
	private InternalConfigDirectory internalConfigDirectory;

	@Test
	public void testInternalConfigDirectory(){
		System.getProperties().forEach((key,value) -> {
			logger.warn("property_" + key + "=" + value);
		});
		System.getenv().forEach((key,value) -> {
			logger.warn("env_" + key + "=" + value);
		});
		logger.warn("internalConfigDirectory={}", internalConfigDirectory.get());
		Assert.assertNotEquals("production", internalConfigDirectory.get());
	}

	@Test
	public void isInTestng(){
		Assert.assertTrue(TestDetector.isTestNg());
	}

}
