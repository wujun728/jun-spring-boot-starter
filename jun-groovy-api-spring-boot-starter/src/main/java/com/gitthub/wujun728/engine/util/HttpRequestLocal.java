package com.gitthub.wujun728.engine.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  保留用户会话，以方便在业务代码任何地方调用
 * @author wujun
 *
 */
@Slf4j
@Component
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



	public static Map<String, Object> getAllParameters(HttpServletRequest request) {
		HttpRequestLocal.setRequest(request);
		String unParseContentType = request.getContentType();

		// 如果是浏览器get请求过来，取出来的contentType是null
		if (unParseContentType == null) {
			unParseContentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
		}
		// 解析contentType 格式: appliation/json;charset=utf-8
		String[] contentTypeArr = unParseContentType.split(";");
		String contentType = contentTypeArr[0];
		Map<String, Object> params = null;
		// 如果是application/json请求，不管接口规定的content-type是什么，接口都可以访问，且请求参数都以json body 为准
		if (contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
			params = getHttpJsonParams(request);
		}
		else {
			params = getHttpJsonParams(request);
		}
		String uri = request.getRequestURI();
		Map<String, String> header = HttpRequestLocal.buildHeaderParams(request);
		Map<String, Object> session = HttpRequestLocal.buildSessionParams(request);
		Map<String, Object> urivar = HttpRequestLocal.getParam(request);
		String pattern = HttpRequestLocal.buildPattern(request);
		Map<String, String> pathvar = HttpRequestLocal.getPathVar(pattern, uri);
		Map<String, Object> params1 = HttpRequestLocal.getFromParams(request);
		if (!CollectionUtils.isEmpty(session)) {
			params.putAll(session);
		}
		if (!CollectionUtils.isEmpty(header)) {
			params.putAll(header);
		}
		if (!CollectionUtils.isEmpty(pathvar)) {
			params.putAll(pathvar);
		}
		if (!CollectionUtils.isEmpty(urivar)) {
			params.putAll(urivar);
		}
		if (!CollectionUtils.isEmpty(params1)) {
			params.putAll(params1);
		}
		params.put("path",uri);
		return params;
	}




	@Deprecated
	public static JSONObject getHttpJsonBody(HttpServletRequest request) {
		try {
			InputStreamReader in = new InputStreamReader(request.getInputStream(), "utf-8");
			BufferedReader br = new BufferedReader(in);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			JSONObject jsonObject = JSON.parseObject(sb.toString());
			return jsonObject;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}
		return null;
	}


	public static Map<String,Object> getHttpJsonParams(HttpServletRequest request) {
		try {
			ServletRequest requestWrapper = new RequestWrapper((HttpServletRequest) request);
			Map<String,Object> params = new HashMap<>();
			JSONObject jsonObject = new JSONObject();
			InputStreamReader in = new InputStreamReader(requestWrapper.getInputStream(), "utf-8");
			BufferedReader br = new BufferedReader(in);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			jsonObject = JSON.parseObject(sb.toString());

			if (!ObjectUtils.isEmpty(jsonObject)) {
				params = JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, Object>>() {
				});
			}
			return params;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}
		return null;
	}


	public static Map<String, String> buildHeaderParams(HttpServletRequest request)
		/* throws UnsupportedEncodingException */ {
		Enumeration<String> headerKeys = request.getHeaderNames();
		Map<String, String> result = new HashMap<>();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			String value = request.getHeader(key);
			result.put(key, value);
		}
		return result;
	}

	public static Map<String, Object> buildSessionParams(HttpServletRequest request) {
		Enumeration<String> keys = request.getSession().getAttributeNames();
		Map<String, Object> result = new HashMap<>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			result.put(key, request.getSession().getAttribute(key));
		}
		return result;
	}

	public static Map<String, Object> getParam(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		if (request.getQueryString() != null) {
			url.append("?");
			url.append(request.getQueryString());
		}
		Map<String, Object> result = new HashMap<>();
		MultiValueMap<String, String> urlMvp = UriComponentsBuilder.fromHttpUrl(url.toString()).build().getQueryParams();
		urlMvp.forEach((key, value) -> {
			String firstValue = CollectionUtils.isEmpty(value) ? null : value.get(0);
			result.put(key, firstValue);
		});
		return result;
	}


	public static String buildPattern(HttpServletRequest request) {
		return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	}



	public static Map<String, String> getPathVar(String pattern, String url) {
		Integer beginIndex = url.indexOf("/", 8);
		if (beginIndex == -1) {
			return null;
		}
		Integer endIndex = url.indexOf("?") == -1 ? url.length() : url.indexOf("?");
		String path = url.substring(beginIndex, endIndex);
		AntPathMatcher matcher = new AntPathMatcher();
		if (matcher.match(pattern, path)) {
			return matcher.extractUriTemplateVariables(pattern, path);
		}
		return null;
	}




	public static Map<String, Object> getFromParams(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length > 0) {
				String paramValue = paramValues[0];
				if (paramValue.length() != 0) {
					map.put(paramName, paramValue);
				}
			}
		}
		Set<Map.Entry<String, Object>> set = map.entrySet();
		log.debug("==============================================================");
		for (Map.Entry entry : set) {
			log.debug(entry.getKey() + ":" + entry.getValue());
		}
		log.debug("=============================================================");
		return map;
	}


	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************


	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 获取body
	 *
	 * @param req
	 * @return
	 */
	public static Map<String, Object> getBodyMap(HttpServletRequest req) {
		BufferedReader reader = null;
		Map<String, Object> bodyMap = new HashMap<>();
		try {
			reader = req.getReader();
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				builder.append(line);
				line = reader.readLine();
			}
			reader.close();
			String bodyString = builder.toString();
			if (!"".equals(bodyString)) {
				bodyMap = objectMapper.readValue(bodyString, Map.class);
			}
		} catch (Exception e) {

		}
		return bodyMap;
	}

	/**
	 * 获取 params 参数
	 *
	 * @param req
	 * @return
	 */

	public static Map<String, Object> getParameters(HttpServletRequest req) {
		Map<String, Object> params = new HashMap<>();
		Map<String, String[]> parameterMap = req.getParameterMap();
		if (parameterMap.isEmpty()) {
			return params;
		}
		Set<String> keys = parameterMap.keySet();
		for (String key : keys) {
			String[] values = parameterMap.get(key);
			if (values.length == 1) {
				params.put(key, values[0]);
			} else {
				params.put(key, values);
			}
		}
		return params;
	}

	/**
	 * 获取请求头和URL参数中的认证code
	 *
	 * @param req
	 * @return
	 */
	public static String getAppCode(HttpServletRequest req) {
		String appCode = req.getHeader("appCode");
		if (appCode == null) {
			String[] codeNames = new String[]{"appCode","AppCode", "appcode", "app_code"};
			for(String codeName : codeNames) {
				appCode = req.getParameter(codeName);
				if (appCode != null) {
					break;
				}
			}
		}
		return appCode;
	}

	/**
	 * 获取IP地址
	 *
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
			if (ip.indexOf(",") != -1) {
				ip = ip.split(",")[0];
			}
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip == null ? "" : ip.trim();
	}

	/**
	 * 根据关键字解析json
	 *
	 * @param @param jsonObject
	 * @param @param keyName 支持逗号分隔
	 * @return Object
	 */
	public static Object decodeJsonObject(String jsonObject, String keyName) {
		Object result = null;
		if (jsonObject == null || "".equals(jsonObject)) {
			return null;
		}
		JsonFactory jasonFactory = new JsonFactory();
		JsonParser parser = null;
		try {
			parser = jasonFactory.createParser(jsonObject);
			JsonToken firstToken = parser.nextToken();
			if (!JsonToken.START_OBJECT.equals(firstToken)) {
				return result;
			}
			while (!parser.isClosed()) {
				JsonToken t = parser.nextToken();
				if (JsonToken.FIELD_NAME.equals(t) && keyName.equals(parser.getCurrentName())) {
					JsonToken v = parser.nextToken();
					if (JsonToken.VALUE_NULL.equals(v)) {
						return null;
					} else if (JsonToken.VALUE_STRING.equals(v)) {
						return parser.getValueAsString();
					} else if (JsonToken.VALUE_TRUE.equals(v) || JsonToken.VALUE_FALSE.equals(v)) {
						return parser.getBooleanValue();
					} else if (JsonToken.VALUE_NUMBER_INT.equals(v)) {
						return parser.getLongValue();
					} else if (JsonToken.VALUE_NUMBER_FLOAT.equals(v)) {
						return parser.getDoubleValue();
					}
				} else if (JsonToken.START_OBJECT.equals(t) || JsonToken.START_ARRAY.equals(t)) {
					parser.skipChildren();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (parser != null) {
					parser.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}