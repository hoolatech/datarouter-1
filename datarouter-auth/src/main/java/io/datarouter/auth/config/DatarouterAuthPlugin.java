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
package io.datarouter.auth.config;

import java.util.List;

import io.datarouter.auth.service.DatarouterUserInfo;
import io.datarouter.auth.service.DefaultDatarouterAccountKeys;
import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.auth.service.DefaultDatarouterUserPassword;
import io.datarouter.auth.service.DefaultDatarouterUserPasswordSupplier;
import io.datarouter.auth.service.UserInfo;
import io.datarouter.auth.service.deprovisioning.DatarouterUserDeprovisioningStrategy;
import io.datarouter.auth.service.deprovisioning.UserDeprovisioningListeners;
import io.datarouter.auth.service.deprovisioning.UserDeprovisioningListeners.EmptyUserDeprovisioningListeners;
import io.datarouter.auth.service.deprovisioning.UserDeprovisioningStrategy;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao.DatarouterAccountDaoParams;
import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionDao.DatarouterAccountPermissionDaoParams;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao.DeprovisionedUserDaoParams;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao.DatarouterPermissionRequestDaoParams;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.user.DatarouterUserDao.DatarouterUserDaoParams;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao.DatarouterUserAccountMapDaoParams;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao.DatarouterUserHistoryDaoParams;
import io.datarouter.auth.web.DatarouterDocumentationRouteSet;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.job.config.DatarouterJobRouteSet;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.AppNavBarCategory;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.authenticate.saml.BaseDatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao.DatarouterSamlDaoParams;

public class DatarouterAuthPlugin extends BaseJobPlugin{

	private static final DatarouterAuthPaths PATHS = new DatarouterAuthPaths();

	private final Class<? extends UserInfo> userInfoClass;
	private final Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass;
	private final Class<? extends UserDeprovisioningListeners> userDeprovisioningListenersClass;
	private final String defaultDatarouterUserPassword;
	private final String defaultApiKey;
	private final String defaultSecretKey;

	private DatarouterAuthPlugin(
			boolean enableUserAuth,
			DatarouterAuthDaoModule daosModuleBuilder,
			Class<? extends UserInfo> userInfoClass,
			Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass,
			Class<? extends UserDeprovisioningListeners> userDeprovisioningListenersClass,
			String defaultDatarouterUserPassword,
			String defaultApiKey,
			String defaultSecretKey){
		this.userInfoClass = userInfoClass;
		this.userDeprovisioningStrategyClass = userDeprovisioningStrategyClass;
		this.userDeprovisioningListenersClass = userDeprovisioningListenersClass;
		this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
		this.defaultApiKey = defaultApiKey;
		this.defaultSecretKey = defaultSecretKey;

		if(enableUserAuth){
			addAppListener(DatarouterUserConfigAppListener.class);
			addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.admin.viewUsers, "View Users");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.editUser, "Edit User");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.permissionRequest, "Permission Request");
			addDynamicNavBarItem(CreateUserNavBarItem.class);
			addRouteSetOrdered(DatarouterAuthRouteSet.class, DatarouterJobRouteSet.class);
		}

		addAppListener(DatarouterAccountConfigAppListener.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.KEYS, PATHS.admin.accounts, "Account Manager");
		addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.userDeprovisioning, "User Deprovisioning");
		addDynamicNavBarItem(ApiDocsNavBarItem.class);
		addRouteSet(DatarouterAccountRouteSet.class);
		addRouteSet(DatarouterDocumentationRouteSet.class);
		addRouteSet(UserDeprovisioningRouteSet.class);
		addSettingRoot(DatarouterAuthSettingRoot.class);
		addTriggerGroup(DatarouterAuthTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-auth");
	}

	@Override
	public String getName(){
		return "DatarouterAuth";
	}

	@Override
	protected void configure(){
		bindActual(BaseDatarouterSessionDao.class, DatarouterSessionDao.class);
		bindActual(BaseDatarouterAccountDao.class, DatarouterAccountDao.class);
		bindActual(BaseDatarouterAccountPermissionDao.class, DatarouterAccountPermissionDao.class);
		bindActual(BaseDatarouterUserAccountMapDao.class, DatarouterUserAccountMapDao.class);
		bindActual(BaseDatarouterSamlDao.class, DatarouterSamlDao.class);
		bind(UserInfo.class).to(userInfoClass);
		bind(UserDeprovisioningStrategy.class).to(userDeprovisioningStrategyClass);
		bindActual(UserDeprovisioningListeners.class, userDeprovisioningListenersClass);
		bindActualInstance(DefaultDatarouterUserPasswordSupplier.class,
				new DefaultDatarouterUserPassword(defaultDatarouterUserPassword));
		bindActualInstance(DefaultDatarouterAccountKeysSupplier.class,
				new DefaultDatarouterAccountKeys(defaultApiKey, defaultSecretKey));
	}

	public static class DatarouterAuthPluginBuilder{

		private final boolean enableUserAuth;
		private final ClientId defaultClientId;

		private Class<? extends UserInfo> userInfoClass = DatarouterUserInfo.class;
		private Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass =
				DatarouterUserDeprovisioningStrategy.class;
		private Class<? extends UserDeprovisioningListeners> userDeprovisioningListenersClass =
				EmptyUserDeprovisioningListeners.class;
		private String defaultDatarouterUserPassword = "";
		private String defaultApiKey = "";
		private String defaultSecretKey = "";

		public DatarouterAuthPluginBuilder(boolean enableUserAuth, ClientId defaultClientId,
				String defaultDatarouterUserPassword, String defaultApiKey, String defaultSecretKey){
			this.enableUserAuth = enableUserAuth;
			this.defaultClientId = defaultClientId;
			this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
			this.defaultApiKey = defaultApiKey;
			this.defaultSecretKey = defaultSecretKey;
		}

		public DatarouterAuthPluginBuilder setUserInfoClass(Class<? extends UserInfo> userInfoClass){
			this.userInfoClass = userInfoClass;
			return this;
		}

		public DatarouterAuthPluginBuilder setUserDeprovisioningStrategyClass(
				Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass){
			this.userDeprovisioningStrategyClass = userDeprovisioningStrategyClass;
			return this;
		}

		public DatarouterAuthPluginBuilder setUserDeprovisioningListenersClass(
				Class<? extends UserDeprovisioningListeners> userDeprovisioningListenersClass){
			this.userDeprovisioningListenersClass = userDeprovisioningListenersClass;
			return this;
		}

		public DatarouterAuthPlugin build(){
			return new DatarouterAuthPlugin(
					enableUserAuth,
					new DatarouterAuthDaoModule(
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId),
					userInfoClass,
					userDeprovisioningStrategyClass,
					userDeprovisioningListenersClass,
					defaultDatarouterUserPassword,
					defaultApiKey,
					defaultSecretKey);
		}

	}

	public static class DatarouterAuthDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterAccountClientId;
		private final ClientId datarouterAccountPermissionClientId;
		private final ClientId datarouterPermissionRequestClientId;
		private final ClientId datarouterSamlClientId;
		private final ClientId datarouterUserAccountMapClientId;
		private final ClientId datarouterUserClientId;
		private final ClientId datarouterUserHistoryClientId;
		private final ClientId deprovisionedUserClientId;

		public DatarouterAuthDaoModule(
				ClientId datarouterAccountClientId,
				ClientId datarouterAccountPermissionClientId,
				ClientId datarouterPermissionRequestClientId,
				ClientId datarouterSamlClientId,
				ClientId datarouterUserAccountMapClientId,
				ClientId datarouterUserClientId,
				ClientId datarouterUserHistoryClientId,
				ClientId deprovisionedUserClientId){
			this.datarouterAccountClientId = datarouterAccountClientId;
			this.datarouterAccountPermissionClientId = datarouterAccountPermissionClientId;
			this.datarouterPermissionRequestClientId = datarouterPermissionRequestClientId;
			this.datarouterSamlClientId = datarouterSamlClientId;
			this.datarouterUserAccountMapClientId = datarouterUserAccountMapClientId;
			this.datarouterUserClientId = datarouterUserClientId;
			this.datarouterUserHistoryClientId = datarouterUserHistoryClientId;
			this.deprovisionedUserClientId = deprovisionedUserClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterAccountDao.class,
					DatarouterAccountPermissionDao.class,
					DatarouterPermissionRequestDao.class,
					DatarouterUserAccountMapDao.class,
					DatarouterUserDao.class,
					DatarouterUserHistoryDao.class,
					DatarouterSamlDao.class,
					DeprovisionedUserDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterUserDaoParams.class)
					.toInstance(new DatarouterUserDaoParams(datarouterUserClientId));
			bind(DatarouterUserHistoryDaoParams.class)
					.toInstance(new DatarouterUserHistoryDaoParams(datarouterUserHistoryClientId));
			bind(DatarouterPermissionRequestDaoParams.class)
					.toInstance(new DatarouterPermissionRequestDaoParams(datarouterPermissionRequestClientId));
			bind(DatarouterAccountDaoParams.class)
					.toInstance(new DatarouterAccountDaoParams(datarouterAccountClientId));
			bind(DatarouterAccountPermissionDaoParams.class)
					.toInstance(new DatarouterAccountPermissionDaoParams(datarouterAccountPermissionClientId));
			bind(DatarouterUserAccountMapDaoParams.class)
					.toInstance(new DatarouterUserAccountMapDaoParams(datarouterUserAccountMapClientId));
			bind(DatarouterSamlDaoParams.class)
					.toInstance(new DatarouterSamlDaoParams(datarouterSamlClientId));
			bind(DeprovisionedUserDaoParams.class)
					.toInstance(new DeprovisionedUserDaoParams(deprovisionedUserClientId));
		}

	}

}
