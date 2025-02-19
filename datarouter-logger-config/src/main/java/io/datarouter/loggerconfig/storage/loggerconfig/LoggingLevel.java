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
package io.datarouter.loggerconfig.storage.loggerconfig;

import org.apache.logging.log4j.Level;

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;

public enum LoggingLevel implements StringEnum<LoggingLevel>{
	ALL(Level.ALL),
	TRACE(Level.TRACE),
	DEBUG(Level.DEBUG),
	INFO(Level.INFO),
	WARN(Level.WARN),
	ERROR(Level.ERROR),
	OFF(Level.OFF),
	;

	private final Level level;

	LoggingLevel(Level level){
		this.level = level;
	}

	public Level getLevel(){
		return level;
	}

	@Override
	public String getPersistentString(){
		return level.name();
	}

	@Override
	public LoggingLevel fromPersistentString(String string){
		return fromString(string);
	}

	public static LoggingLevel fromString(String string){
		return DatarouterEnumTool.getEnumFromString(values(), string, null);
	}

	public static int getSqlSize(){
		return 5;
	}

}
