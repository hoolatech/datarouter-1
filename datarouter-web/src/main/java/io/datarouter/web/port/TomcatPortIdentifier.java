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
package io.datarouter.web.port;

import java.util.ArrayList;

import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.util.MxBeans;

@Singleton
public class TomcatPortIdentifier implements PortIdentifier{

	private Integer httpPort;
	private Integer httpsPort;

	public TomcatPortIdentifier() throws MalformedObjectNameException{
		ObjectName query = new ObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=ProtocolHandler,*");
		var handlers = new ArrayList<String>();
		MxBeans.SERVER.queryNames(query, null).forEach(objectName -> {
			handlers.add(objectName.toString());
			int port;
			String scheme;
			try{
				port = (int)MxBeans.SERVER.getAttribute(objectName, "port");
				objectName = new ObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=Connector,port="
						+ port);
				scheme = (String)MxBeans.SERVER.getAttribute(objectName, "scheme");
			}catch(JMException e){
				throw new RuntimeException(e);
			}
			if(scheme.equals(UrlScheme.HTTPS.getStringRepresentation())){
				httpsPort = port;
			}else if(scheme.equals(UrlScheme.HTTP.getStringRepresentation())){
				httpPort = port;
			}
		});
		if(httpPort == null || httpsPort == null){
			throw new RuntimeException("port not found httpPort=" + httpPort + " httpsPort=" + httpsPort + " handlers="
					+ handlers);
		}
	}

	@Override
	public Integer getHttpPort(){
		return httpPort;
	}

	@Override
	public Integer getHttpsPort(){
		return httpsPort;
	}

}
