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
package io.datarouter.web.homepage;

import javax.inject.Inject;

import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4HomepageCreatorService;

public class AppNavHomepageHandler extends HomepageHandler{

	@Inject
	private Bootstrap4HomepageCreatorService service;

	@Handler(defaultHandler = true)
	public Mav homepage(){
		return service.homepage(request,
				service.headerAndDescription(),
				service.docLinks(),
				service.appNavbarCards());
	}

}
