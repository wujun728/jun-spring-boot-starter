package com.jun.plugin.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 保留用户会话，以方便在业务代码任何地方调用
 *
 * @author wujun
 */
@Slf4j
@Component
public class HttpRequestUtil {

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
		HttpRequestUtil.setRequest(request);
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
		} else {
			params = getHttpJsonParams(request);
		}
		String uri = request.getRequestURI();
		Map<String, String> header = HttpRequestUtil.buildHeaderParams(request);
		Map<String, Object> session = HttpRequestUtil.buildSessionParams(request);
		Map<String, Object> urivar = HttpRequestUtil.getParam(request);
		String pattern = HttpRequestUtil.buildPattern(request);
		Map<String, String> pathvar = HttpRequestUtil.getPathVar(pattern, uri);
		Map<String, Object> params1 = HttpRequestUtil.getFromParams(request);
		String ip = HttpRequestUtil.getIp(request);
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
		params.put("path", uri);
		params.put("ip", ip);
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


	public static Map<String, Object> getHttpJsonParams(HttpServletRequest request) {
		try {
			ServletRequest requestWrapper = new RequestWrapper((HttpServletRequest) request);
			Map<String, Object> params = new HashMap<>();
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			InputStreamReader in = new InputStreamReader(requestWrapper.getInputStream(), "utf-8");
			BufferedReader br = new BufferedReader(in);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			Object jsonObj = JSON.parse(sb.toString());
			if (jsonObj instanceof JSONObject) {
				jsonObject = (JSONObject) jsonObj;
			} else if (jsonObj instanceof JSONArray) {
				jsonArray = (JSONArray) jsonObj;
			}

			if (!ObjectUtils.isEmpty(jsonObject)) {
				//params = JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, Object>>() { });
				traverseJsonTree(jsonObject, "", params);
			} else if (!ObjectUtils.isEmpty(jsonArray)) {
				//List<Map> mapList =  JSONArray.parseArray(jsonObject.toJSONString(),Map.class );
				traverseJsonArray(jsonArray, "", params);
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

	@Deprecated
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

	@Deprecated
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
	@Deprecated
	public static String getAppCode(HttpServletRequest req) {
		String appCode = req.getHeader("appCode");
		if (appCode == null) {
			String[] codeNames = new String[]{"appCode", "AppCode", "appcode", "app_code"};
			for (String codeName : codeNames) {
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


	public static void main(String[] args) {

		String jsonString = "{\n" +
				"    \"name\":\"John\",\n" +
				"    \"age\":[\n" +
				"  {\n" +
				"    \"id\": \"1668160716561219582\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"id\": \"1668160716561219585\"\n" +
				"  }\n" +
				"],\n" +
				"    \"address\":{\n" +
				"        \"city\":\"New York\",\n" +
				"        \"state\":\"NY\",\n" +
				"        \"zip\":\"10001\",\n" +
				"        \"coordinates\":{\n" +
				"            \"latitude\":40.712776,\n" +
				"            \"longitude\":-74.005974,\n" +
				"            \"accuracy\":5,\n" +
				"            \"details\":{\n" +
				"                \"description\":\"Exact location of the building\"\n" +
				"            }\n" +
				"        },\n" +
				"        \"details\":{\n" +
				"            \"street\":\"123 Main St\",\n" +
				"            \"apt\":\"4B\",\n" +
				"            \"building\":{\n" +
				"                \"name\":\"Central Park Tower\",\n" +
				"                \"floors\":98,\n" +
				"                \"amenities\":{\n" +
				"                    \"gym\":true,\n" +
				"                    \"pool\":true,\n" +
				"                    \"parking\":{\n" +
				"                        \"garage\":true,\n" +
				"                        \"valet\":true\n" +
				"                    }\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"}";
		Map params = Maps.newHashMap();
		Object jsonObject = JSON.parse(jsonString);
		traverseJsonTree((JSONObject) jsonObject, "", params);
//			traverseJsonArray((JSONArray) jsonObject,"",params);
		System.out.println(JSON.toJSONString(params));
	}

	public static void traverseJsonTree(JSONObject jsonObject, String parentKey, Map params) {
		for (String key : jsonObject.keySet()) {
			Object object = jsonObject.get(key);
			if (object instanceof JSONObject) {
				traverseJsonTree((JSONObject) object, parentKey + "." + key + "", params);
			} else if (object instanceof JSONArray) {
				traverseJsonArray((JSONArray) object, parentKey + "." + key + "", params);
			} else {
				System.out.println(key + ":" + object);
				System.out.println("---------  " + parentKey + "." + key + ":" + object);
				String longKey = parentKey + "." + key;
				if (Pattern.compile(".*[0-9].*").matcher(longKey).matches()) {
					params.put(longKey, object);
				} else {
					params.put(key, object);
				}
			}
		}
	}

	public static void traverseJsonArray(JSONArray jsonArray, String parentKey, Map params) {
		for (int i = 0; i < jsonArray.size(); i++) {
			Object object = jsonArray.get(i);
			if (object instanceof JSONObject) {
				traverseJsonTree((JSONObject) object, parentKey + "." + i + "", params);
			} else if (object instanceof JSONArray) {
				traverseJsonArray((JSONArray) object, parentKey + "." + i + "", params);
			} else {
				//System.out.println(object);
				System.out.println(parentKey + ":" + i + ":" + object);
			}
		}
	}
	public static byte[] object2bytes(Object obj) throws Exception {
		if (obj == null) {
			return null;
		}
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bo);
		oos.writeObject(obj);
		return bo.toByteArray();
	}
	public static Object bytes2object(byte[] bytes) {
		Object object = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);//
			ObjectInputStream ois = new ObjectInputStream(bis);
			object = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return object;
	}


}
