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
package io.datarouter.conveyor.dto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import io.datarouter.conveyor.Conveyor;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;

public class ConveyorSummary{

	public final String name;
	public final ExecutorService executor;
	public final Conveyor conveyor;

	public ConveyorSummary(String name, ExecutorService executor, Conveyor conveyor){
		this.name = name;
		this.executor = executor;
		this.conveyor = conveyor;
	}

	public static List<ConveyorSummary> summarize(Map<String,Pair<ExecutorService,Conveyor>> entries){
		return Scanner.of(entries.entrySet())
				.map(entry -> new ConveyorSummary(
						entry.getKey(),
						entry.getValue().getLeft(),
						entry.getValue().getRight()))
				.list();
	}

}
