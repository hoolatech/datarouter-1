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

import io.datarouter.util.string.StringTool;

public class ConfigDirectory{

	private static final String BASE_CONFIG_DIRECTORY_ENV_VARIABLE = "BASE_CONFIG_DIRECTORY";
	private static final String DEFAULT_BASE_CONFIG_DIRECTORY = "/etc/datarouter";
	public static final String CONFIG_DIRECTORY;
	public static final String TEST_CONFIG_DIRECTORY;
	public static final String SOURCE;

	static{
		String baseConfigDirectory = System.getenv(BASE_CONFIG_DIRECTORY_ENV_VARIABLE);
		if(StringTool.isEmpty(baseConfigDirectory)){
			baseConfigDirectory = DEFAULT_BASE_CONFIG_DIRECTORY;
			SOURCE = "default constant";
		}else{
			SOURCE = "environment variable";
		}
		CONFIG_DIRECTORY = baseConfigDirectory + "/config";
		TEST_CONFIG_DIRECTORY = baseConfigDirectory + "/test";
	}

}
