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
package io.datarouter.auth.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;

import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.authenticate.DatarouterAuthenticationFilter;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.user.session.service.Role;

/**
 * Implementation of {@link CurrentUserSessionInfo}.
 * Having a {@link io.datarouter.web.user.session.service.Session} set as an {@link ServletRequest} attribute using
 * {@link DatarouterSessionManager} (usually by {@link DatarouterAuthenticationFilter}) is a prerequisite for
 * {@link CurrentUserSessionInfo} functionality.
 */
@Singleton
public class DatarouterCurrentUserSessionInfo implements CurrentUserSessionInfo{

	@Inject
	private DatarouterUserInfo datarouterUserInfo;

	@Override
	public Optional<DatarouterUser> getUser(ServletRequest request){
		return DatarouterSessionManager.getFromRequest(request)
				.map(DatarouterSession::getUserId)
				.flatMap(datarouterUserInfo::getUserById)
				.map(DatarouterCurrentUserSessionInfo::validateEnabled);
	}

	@Override
	public Optional<DatarouterSession> getSession(ServletRequest request){
		return DatarouterSessionManager.getFromRequest(request);
	}

	@Override
	public Set<Role> getRoles(ServletRequest request){
		return getSession(request)
				.map(DatarouterSession::getRoles)
				.map(HashSet::new)
				.orElseGet(HashSet::new);
	}

	private static DatarouterUser validateEnabled(DatarouterUser user){
		if(!BooleanTool.isTrue(user.getEnabled())){
			throw new RuntimeException("Current user is not enabled");
		}
		return user;
	}

}
