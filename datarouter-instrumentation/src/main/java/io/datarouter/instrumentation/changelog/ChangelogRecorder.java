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
package io.datarouter.instrumentation.changelog;

public interface ChangelogRecorder{

	default void record(String changelogType, String name, String action, String username, String userToken){
		record(changelogType, name, action, username, userToken, null);
	}

	void record(String changelogType, String name, String action, String username, String userToken, String comment);

	class NoOpChangelogRecorder implements ChangelogRecorder{

		@Override
		public void record(String changelogType, String name, String action, String username, String userToken,
				String comment){
		}

	}

}
