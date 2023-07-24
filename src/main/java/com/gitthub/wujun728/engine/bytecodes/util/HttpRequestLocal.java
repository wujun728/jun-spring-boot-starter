package com.gitthub.wujun728.engine.bytecodes.util;

import javax.servlet.http.HttpServletRequest;

/**
 *  保留用户会话，以方便在业务代码任何地方调用
 *  {@link}
 * @author lijiazhi
 *
 */
public class HttpRequestLocal {

	private static final ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>() {
		@Override
		protected HttpServletRequest initialValue() {
			return null;
		}
	};


	public static HttpServletRequest getRequest() {
		return requests.get();
	}


	public static void setRequest(HttpServletRequest request) {
		requests.set(request);
	}

}
