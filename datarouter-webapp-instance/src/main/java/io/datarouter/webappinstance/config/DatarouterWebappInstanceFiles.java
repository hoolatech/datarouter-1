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
package io.datarouter.webappinstance.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.PathNode;

@Singleton
public class DatarouterWebappInstanceFiles extends FilesRoot{

	public final JspFiles jsp = branch(JspFiles::new, "jsp");

	public static class JspFiles extends PathNode{
		public final AdminFiles admin = branch(AdminFiles::new, "admin");
	}

	public static class AdminFiles extends PathNode{
		public final DatarouterAdminFiles datarouter = branch(DatarouterAdminFiles::new, "datarouter");
	}

	public static class DatarouterAdminFiles extends PathNode{
		public final WebappInstanceFiles webappInstances = branch(WebappInstanceFiles::new, "webappInstances");
	}

	public static class WebappInstanceFiles extends PathNode{
		public final PathNode webappInstanceJsp = leaf("webappInstances.jsp");
		public final PathNode webappInstanceLogJsp = leaf("webappInstanceLog.jsp");
	}

}
