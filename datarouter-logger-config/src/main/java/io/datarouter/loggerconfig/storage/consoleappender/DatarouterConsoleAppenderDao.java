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
package io.datarouter.loggerconfig.storage.consoleappender;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import io.datarouter.loggerconfig.storage.consoleappender.ConsoleAppender.ConsoleAppenderFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterConsoleAppenderDao extends BaseDao{

	public static class DatarouterConsoleAppenderDaoParams extends BaseDaoParams{

		public DatarouterConsoleAppenderDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<ConsoleAppenderKey,ConsoleAppender> node;

	@Inject
	public DatarouterConsoleAppenderDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterConsoleAppenderDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, ConsoleAppender::new, ConsoleAppenderFielder::new)
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public Scanner<ConsoleAppender> scan(){
		return node.scan();
	}

	public void createAndPutConsoleAppender(String name, String pattern, Target target){
		var appender = new ConsoleAppender(name, pattern, target);
		node.put(appender);
	}

	public void deleteConsoleAppender(String name){
		node.delete(new ConsoleAppenderKey(name));
	}

}
