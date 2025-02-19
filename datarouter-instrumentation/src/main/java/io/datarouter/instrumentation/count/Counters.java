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
package io.datarouter.instrumentation.count;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Static class for incrementing counts throughout an application.  Register with this class to receive counts.
 */
public class Counters{

	public static final long MS_IN_SECOND = Duration.ofSeconds(1).toMillis();
	public static final long MS_IN_MINUTE = Duration.ofMinutes(1).toMillis();
	public static final long MS_IN_HOUR = Duration.ofHours(1).toMillis();
	public static final long MS_IN_DAY = Duration.ofDays(1).toMillis();

	public static String getSuffix(long periodMs){
		if(periodMs >= MS_IN_DAY){
			return periodMs / MS_IN_DAY + "d";
		}else if(periodMs >= MS_IN_HOUR){
			return periodMs / MS_IN_HOUR + "h";
		}else if(periodMs >= MS_IN_MINUTE){
			return periodMs / MS_IN_MINUTE + "m";
		}else if(periodMs >= MS_IN_SECOND){
			return periodMs / MS_IN_SECOND + "s";
		}else{
			throw new IllegalArgumentException("unknown duration:" + periodMs);
		}
	}

	private static final ArrayList<CountCollector> COLLECTORS = new ArrayList<>();

	/*---------- admin -------------*/

	public static void addCollector(CountCollector collector){
		COLLECTORS.add(collector);
	}

	public static void stopAndFlushAll(){
		for(int i = 0; i < COLLECTORS.size(); ++i){
			COLLECTORS.get(i).stopAndFlushAll();
		}
	}

	/*------------ couting ----------*/

	public static void inc(String key){
		inc(key, 1);
	}

	public static void inc(String key, long delta){
		for(int i = 0; i < COLLECTORS.size(); ++i){
			COLLECTORS.get(i).increment(key, delta);
		}
	}

}
