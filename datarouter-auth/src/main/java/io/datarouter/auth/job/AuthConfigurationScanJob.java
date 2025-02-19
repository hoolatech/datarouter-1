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
package io.datarouter.auth.job;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.auth.service.DatarouterAuthConfigScanner;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.service.ConfigurationScanReportService;
import io.datarouter.web.autoconfig.ConfigScanDto;

public class AuthConfigurationScanJob extends BaseJob{

	@Inject
	private DatarouterAuthConfigScanner configScanner;
	@Inject
	private ConfigurationScanReportService reportService;

	@Override
	public void run(TaskTracker tracker){
		List<ConfigScanDto> scans = List.of(
				configScanner.checkDatarouterAccountsWithDefaultKeys(),
				configScanner.checkForDefaultUserId());
		reportService.scanConfigurationAndSendEmail("AuthConfig Alert", scans);
	}

}
