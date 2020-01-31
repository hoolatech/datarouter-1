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
package io.datarouter.webappinstance.storage.webappinstancelog;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog.WebappInstanceLogFielder;

@Singleton
public class DatarouterWebappInstanceLogDao extends BaseDao{

	public static class DatarouterWebappInstanceLogDaoParams extends BaseDaoParams{

		public DatarouterWebappInstanceLogDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<WebappInstanceLogKey,WebappInstanceLog> node;

	@Inject
	public DatarouterWebappInstanceLogDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterWebappInstanceLogDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, WebappInstanceLog::new, WebappInstanceLogFielder::new)
				.buildAndRegister();
	}

	public void put(WebappInstanceLog log){
		node.put(log);
	}

	public Scanner<WebappInstanceLog> scanWithPrefix(WebappInstanceLogKey key){
		return node.scanWithPrefix(key);
	}

}
