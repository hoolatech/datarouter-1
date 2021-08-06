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
package io.datarouter.scanner;

import java.util.function.UnaryOperator;

public class IteratingScanner<T> extends BaseScanner<T>{

	private final T seed;
	private final UnaryOperator<T> unaryOperator;
	private boolean started = false;

	public IteratingScanner(T seed, UnaryOperator<T> unaryOperator){
		this.seed = seed;
		this.unaryOperator = unaryOperator;
	}

	@Override
	public boolean advance(){
		if(!started){
			current = seed;
			started = true;
		}else{
			current = unaryOperator.apply(current);
		}
		return true;
	}

}
