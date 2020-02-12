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
package io.datarouter.web.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.io.FileTool;

@Singleton
public class StaticFileFilter implements Filter{
	private static final Logger logger = LoggerFactory.getLogger(StaticFileFilter.class);

	private FilterConfig filterConfig;
	private RequestDispatcher requestDispatcher;

	@Override
	public void init(FilterConfig filterConfig){
		this.filterConfig = filterConfig;
		this.requestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
	throws IOException, ServletException{

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		String path = request.getRequestURI();

		if(!FileTool.hasAStaticFileExtension(path)){
			filterChain.doFilter(request, response);
			return;
		}
		Enumeration<String> initParameterNames = filterConfig.getInitParameterNames();
		while(initParameterNames.hasMoreElements()){
			String headerName = initParameterNames.nextElement();
			String headerValue = filterConfig.getInitParameter(headerName);
			response.addHeader(headerName, headerValue);
		}

		long startForward = System.currentTimeMillis();
		requestDispatcher.forward(request, response);
		long endForward = System.currentTimeMillis();
	}

}
