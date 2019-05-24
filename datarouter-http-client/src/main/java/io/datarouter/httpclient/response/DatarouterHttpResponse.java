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
package io.datarouter.httpclient.response;

import java.util.List;
import java.util.function.Consumer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;

import io.datarouter.httpclient.client.HttpRetryTool;

/**
 * This class is an abstraction over the HttpResponse that handles several of the expected HTTP failures
 */
public class DatarouterHttpResponse{

	private final HttpResponse response;
	private final List<Cookie> cookies;

	private int statusCode;
	private String entity;

	public DatarouterHttpResponse(HttpResponse response, HttpClientContext context,
			Consumer<HttpEntity> httpEntityConsumer){
		this.response = response;
		this.cookies = context.getCookieStore().getCookies();
		if(response != null){
			this.statusCode = response.getStatusLine().getStatusCode();

			HttpEntity httpEntity = response.getEntity();
			if(httpEntity == null){
				return;
			}
			if(httpEntityConsumer != null){
				httpEntityConsumer.accept(httpEntity);
				return;
			}
			this.entity = HttpRetryTool.entityToString(httpEntity).orElse("");
		}
	}

	public int getStatusCode(){
		return statusCode;
	}

	public String getEntity(){
		return entity;
	}

	public Header getFirstHeader(String name){
		return response.getFirstHeader(name);
	}

	public Header[] getHeaders(String name){
		return response.getHeaders(name);
	}

	public List<Cookie> getCookies(){
		return cookies;
	}

}
