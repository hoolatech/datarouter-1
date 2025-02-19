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
package io.datarouter.web.digest;

import static j2html.TagCreator.a;
import static j2html.TagCreator.h3;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.config.service.DomainFinder;
import j2html.tags.ContainerTag;

// helper class for making html fragments
@Singleton
public class DailyDigestService{

	@Inject
	private DomainFinder domainFinder;
	@Inject
	private ServletContextSupplier servletContext;

	public ContainerTag<?> makeHeader(String title, PathNode path, String pathSupplement){
		String link = "https://" + domainFinder.getDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "") + pathSupplement;
		return makeHeader(title, link);
	}

	public ContainerTag<?> makeHeader(String title, PathNode path){
		String link = "https://" + domainFinder.getDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "");
		return makeHeader(title, link);
	}

	public ContainerTag<?> makeHeader(String title, String url){
		return h3(a(title)
				.withHref(url));
	}

	public ContainerTag<?> makeATagLink(String title, PathNode path){
		String link = "https://" + domainFinder.getDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "");
		return a(title).withHref(link);
	}

	public ContainerTag<?> makeATagLink(String title, String path){
		String link = "https://" + domainFinder.getDomainPreferPublic() + servletContext.get().getContextPath() + path;
		return a(title).withHref(link);
	}

}
