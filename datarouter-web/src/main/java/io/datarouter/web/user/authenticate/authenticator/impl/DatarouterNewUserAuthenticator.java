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
package io.datarouter.web.user.authenticate.authenticator.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.web.user.authenticate.DatarouterTokenGenerator;
import io.datarouter.web.user.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;

@Singleton
public class DatarouterNewUserAuthenticator implements DatarouterAuthenticator{

	@Inject
	private DatarouterSessionManager sessionManager;

	@Override
	public DatarouterSession getSession(HttpServletRequest request, HttpServletResponse response){
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		DatarouterSession session = DatarouterSession.createAnonymousSession(userToken);
		sessionManager.addUserTokenCookie(response, session.getUserToken());
		sessionManager.addSessionTokenCookie(response, session.getSessionToken());
		return session;
	}

}
