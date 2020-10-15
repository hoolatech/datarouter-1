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
package io.datarouter.web.digest;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class DailyDigestHandler extends BaseHandler{

	@Inject
	private DailyDigestRegistry dailyDigestRegistry;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterInjector injector;

	@Handler(defaultHandler = true)
	public Mav view(){
		List<? extends DailyDigest> digests = Scanner.of(dailyDigestRegistry.registry)
				.map(injector::getInstance)
				.include(dailyDigest -> dailyDigest.getPageContent().isPresent())
				.sorted(Comparator.comparing(DailyDigest::getTitle))
				.list();

		ContainerTag content;
		if(digests.size() == 0){
			content = div("No content for the daily digest.")
					.withClass("container-fluid");
		}else{
			ContainerTag toc = ul(each(digests, digest -> {
				return li(a(digest.getTitle()).withHref("#" + digest.getId()));
			}));
			content = div(toc, each(digests, digest -> div(digest.getPageContent().get()).withId(digest.getId())))
					.withClass("container-fluid");
		}
		return pageFactory.startBuilder(request)
				.withTitle("Daily Digest")
				.withContent(content)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

}
