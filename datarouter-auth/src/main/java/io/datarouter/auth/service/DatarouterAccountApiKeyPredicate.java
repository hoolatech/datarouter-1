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
package io.datarouter.auth.service;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.dispatcher.ApiKeyPredicate;
import io.datarouter.web.dispatcher.DispatchRule;

@Singleton
public class DatarouterAccountApiKeyPredicate extends ApiKeyPredicate{

	private final DatarouterAccountCredentialService datarouterAccountCredentialService;
	private final DatarouterAccountCounters datarouterAccountCounters;

	@Inject
	public DatarouterAccountApiKeyPredicate(
			DatarouterAccountCredentialService datarouterAccountApiKeyService,
			DatarouterAccountCounters datarouterAccountCounters){
		this(SecurityParameters.API_KEY, datarouterAccountApiKeyService, datarouterAccountCounters);
	}

	public DatarouterAccountApiKeyPredicate(String apiKeyFieldName,
			DatarouterAccountCredentialService datarouterAccountCredentialService,
			DatarouterAccountCounters datarouterAccountCounters){
		super(apiKeyFieldName);
		this.datarouterAccountCredentialService = datarouterAccountCredentialService;
		this.datarouterAccountCounters = datarouterAccountCounters;
	}

	@Override
	public Pair<Boolean,String> innerCheck(DispatchRule rule, HttpServletRequest request, String apiKeyCandidate){
		Optional<String> endpoint = rule.getPersistentString();
		return check(endpoint, apiKeyCandidate)
				.map(accountName -> new Pair<>(true, accountName))
				.orElseGet(() -> new Pair<>(false, "no account for " + obfuscate(apiKeyCandidate)));
	}

	public Optional<String> check(Optional<String> endpoint, String apiKeyCandidate){
		Optional<DatarouterAccountPermissionKey> permission = datarouterAccountCredentialService
				.scanPermissionsForApiKeyAuth(apiKeyCandidate)
				.include(candidate -> isValidEndpoint(candidate, endpoint))
				.findFirst();
		permission.ifPresent(datarouterAccountCounters::incPermissionUsage);
		return permission
				.map(DatarouterAccountPermissionKey::getAccountName);
	}

	private boolean isValidEndpoint(DatarouterAccountPermissionKey candidate, Optional<String> endpoint){
		boolean isWildcard = candidate.getEndpoint().equals(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		boolean matches = endpoint
				.map(candidate.getEndpoint()::equals)
				.orElse(false);
		return isWildcard || matches;
	}

}
