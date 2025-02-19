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
package io.datarouter.client.hbase.util;

import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.ServerName;

import io.datarouter.util.lang.ReflectionTool;

public class ServerNameTool{

	public static ServerName create(String hostname, int port, long startcode){
		Collection<?> constructorParams = List.of(hostname, port, startcode);
		return ReflectionTool.createWithParameters(ServerName.class, constructorParams);
	}

	public static ServerName create(String serverName){
		Collection<?> constructorParams = List.of(serverName);
		return ReflectionTool.createWithParameters(ServerName.class, constructorParams);
	}

}
