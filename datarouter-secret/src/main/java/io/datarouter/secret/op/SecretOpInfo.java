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
package io.datarouter.secret.op;

import java.util.Optional;

import io.datarouter.util.lang.ObjectTool;

public class SecretOpInfo{

	public final SecretOpType op;
	public final String namespace;
	public final String name;
	public final SecretOpReason reason;
	public final boolean shouldRecord;
	public final boolean shouldLog;
	public final Optional<String> targetSecretClientConfig;

	public SecretOpInfo(SecretOpType op, String namespace, String name, SecretOpReason reason){
		this(op, namespace, name, reason, false, true);
	}

	public SecretOpInfo(SecretOpType op, String namespace, String name, SecretOpReason reason, boolean shouldRecord,
			boolean shouldLog){
		this(op, namespace, name, reason, shouldRecord, shouldLog, Optional.empty());
	}

	public SecretOpInfo(SecretOpType op, String namespace, String name, SecretOpReason reason,
			Optional<String> targetSecretClientConfig){
		this(op, namespace, name, reason, false, true, targetSecretClientConfig);
	}

	public SecretOpInfo(SecretOpType op, String namespace, String name, SecretOpReason reason, boolean shouldRecord,
			boolean shouldLog, Optional<String> targetSecretClientConfig){
		ObjectTool.requireNonNulls(op, namespace, name, reason, targetSecretClientConfig);

		this.op = op;
		this.namespace = namespace;
		this.name = name;
		this.reason = reason;
		this.shouldRecord = shouldRecord;
		this.shouldLog = shouldLog;
		this.targetSecretClientConfig = targetSecretClientConfig;
	}

	public String getNamespaced(){
		return namespace + name;
	}

	@Override
	public String toString(){
		return String.format("{op=%s, namespace=%s, name=%s, reason=\"%s\" targetSecretClientConfig=%s}", op.name(),
				namespace, name, reason, targetSecretClientConfig.orElse(""));
	}

}