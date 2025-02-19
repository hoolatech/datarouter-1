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
package io.datarouter.web.app;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.config.ServletContextSupplier;

@Singleton
public class ApplicationPaths{

	private final Supplier<String> path;
	private final Supplier<String> resourcesPath;
	private final Supplier<String> webInfPath;

	@Inject
	public ApplicationPaths(ServletContextSupplier servletContextSupplier){
		ServletContext servletContext = servletContextSupplier.get();
		this.path = SingletonSupplier.of(() -> WebappTool.getApplicationRootPath(servletContext));
		this.resourcesPath = SingletonSupplier.of(() -> WebappTool.getResourcesPath(servletContext));
		this.webInfPath = SingletonSupplier.of(() -> WebappTool.getWebInfPath(servletContext));
	}

	public String getRootPath(){
		return path.get();
	}

	public String getResourcesPath(){
		return resourcesPath.get();
	}

	public String getWebInfPath(){
		return webInfPath.get();
	}

}
