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
package io.datarouter.web.user.authenticate;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.httpclient.security.SecureRandomTool;

public class DatarouterTokenGenerator{

	private static final SecureRandom SECURE_RANDOM = SecureRandomTool.getInstance("SHA1PRNG");

	public static String generateRandomToken(){
		byte[] sha1Bytes = new byte[32];
		SECURE_RANDOM.nextBytes(sha1Bytes);
		byte[] sha256Bytes = DigestUtils.sha256(sha1Bytes);//further encode older sha1
		byte[] base64Bytes = Base64.encodeBase64URLSafe(sha256Bytes);
		return StringCodec.UTF_8.decode(base64Bytes);
	}

}
