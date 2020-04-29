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
package io.datarouter.storage.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.model.key.primary.PrimaryKey;

public class EncodedPrimaryKeyPercentCodec{

	public final String nodeName;
	public final Integer databeanVersion;
	public final PrimaryKey<?> primaryKey;

	public EncodedPrimaryKeyPercentCodec(String nodeName, Integer databeanVersion, PrimaryKey<?> primaryKey){
		if(nodeName.contains(":")){
			throw new IllegalArgumentException("nodeName cannot contain \":\"");
		}
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}

	public String getVersionedKeyString(){
		String encodedPk = PrimaryKeyPercentCodecTool.encode(primaryKey);
		return getEncodingVersion() + ":" + nodeName + ":" + databeanVersion + ":" + encodedPk;
	}

	public static List<String> getVersionedKeyStrings(
			String nodeName,
			int version,
			Collection<? extends PrimaryKey<?>> pks){
		return pks.stream()
				.filter(Objects::nonNull)
				.map(pk -> new EncodedPrimaryKeyPercentCodec(nodeName, version, pk))
				.map(EncodedPrimaryKeyPercentCodec::getVersionedKeyString)
				.collect(Collectors.toList());
	}

	public static <PK extends PrimaryKey<PK>> EncodedPrimaryKeyPercentCodec parse(String string, Class<PK> pkClass){
		StringBuilder current = new StringBuilder();
		String[] parts = new String[3];
		int counter = 0;
		for(int i = 0; i < string.length(); i++){
			char character = string.charAt(i);
			if(character == ':'){
				parts[counter++] = current.toString();
				current = new StringBuilder();
			}else{
				current.append(character);
			}
		}
		if(counter != 3){
			throw new RuntimeException("incorrect number of parts, counter=" + counter + " input=" + string);
		}
		int databeanVersion = Integer.parseInt(parts[2]);
		PK primaryKey = PrimaryKeyPercentCodecTool.decode(pkClass, current.toString());
		return new EncodedPrimaryKeyPercentCodec(parts[1], databeanVersion, primaryKey);
	}

	protected Integer getEncodingVersion(){
		return 1;
	}

}
