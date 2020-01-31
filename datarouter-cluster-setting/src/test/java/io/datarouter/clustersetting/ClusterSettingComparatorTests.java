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
package io.datarouter.clustersetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.storage.servertype.ServerType;

public class ClusterSettingComparatorTests{

	@Test
	public void testClusterSettingComparator(){
		List<ClusterSetting> settings = new ArrayList<>();
		ClusterSetting serverType = new ClusterSetting("dev1", ClusterSettingScope.SERVER_TYPE,
				ServerType.DEV.getPersistentString(), "", "", "");
		settings.add(serverType);
		ClusterSetting serverName = new ClusterSetting("instance1", ClusterSettingScope.SERVER_NAME,
				ServerType.UNKNOWN.getPersistentString(), "mySevrer", "", "");
		settings.add(serverName);
		Assert.assertEquals(Collections.min(settings, new ClusterSettingScopeComparator()), serverName);
		ClusterSetting app = new ClusterSetting("instance1", ClusterSettingScope.APPLICATION,
				ServerType.UNKNOWN.getPersistentString(), "", "myApp", "");
		settings.add(app);
		Assert.assertEquals(Collections.min(settings, new ClusterSettingScopeComparator()), app);
	}

}
