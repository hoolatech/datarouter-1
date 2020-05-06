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
package io.datarouter.instrumentation.serviceconfig;

import java.util.Set;

public class ServiceConfigurationDto{

	public final String serviceName;
	public final Set<String> administratorsEmails;
	public final String serviceDescription;


	public ServiceConfigurationDto(String serviceName, Set<String> administratorsEmails,
			String serviceDescription){
		this.serviceName = serviceName;
		this.administratorsEmails = administratorsEmails;
		this.serviceDescription = serviceDescription;
	}

}
