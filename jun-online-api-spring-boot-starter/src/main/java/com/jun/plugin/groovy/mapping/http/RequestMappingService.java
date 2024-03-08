package com.jun.plugin.groovy.mapping.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.extra.spring.SpringUtil;
import com.jun.plugin.groovy.common.model.ApiConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

//import cn.hutool.core.lang.Console;
//import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RequestMappingService implements InitializingBean {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	@Lazy
	private RequestMappingExecutor mappingFactory;

	/**
	 * 获取已注册的API地址
	 */
	public List<ApiConfig> getPathListForCode() {

		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		List<ApiConfig> result = new ArrayList<>(map.size());
		for (RequestMappingInfo info : map.keySet()) {

			if (map.get(info).getMethod().getDeclaringClass() == RequestMappingExecutor.class) {
				continue;
			}

			String groupName = map.get(info).getBeanType().getSimpleName();
			String context = SpringUtil.getProperty("project.groovy-api.context");
			String servicename = SpringUtil.getProperty("project.groovy-api.servicename");
			for (String path : getPatterns(info)) {
				// 过滤本身的类
				if (path.indexOf(context) == 0 || path.equals("/error")) {
					continue;
				}

				Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
				if (methods.isEmpty()) {
					ApiConfig apiInfo = new ApiConfig();
					apiInfo.setPath(path);
					apiInfo.setMethod("All");
					apiInfo.setScriptType("Code");
					apiInfo.setBeanName(servicename);
					apiInfo.setCreator("admin");
					apiInfo.setDatasourceId("");
					apiInfo.setScriptContent("");
					apiInfo.setPath(path);
					result.add(apiInfo);
				} else {
					for (RequestMethod method : methods) {
						ApiConfig apiInfo = new ApiConfig();
						apiInfo.setPath(path);
						apiInfo.setMethod(method.name());
						apiInfo.setScriptType("Code");
						apiInfo.setBeanName(servicename);
						apiInfo.setCreator("admin");
						apiInfo.setDatasourceId("");
						apiInfo.setScriptContent("");
						apiInfo.setPath(path);
						result.add(apiInfo);
					}
				}

			}
		}
		return result;
	}
	


	/**
	 * 注册mapping
	 *
	 * @param apiInfo
	 */
	public synchronized void registerMappingForApiConfig(ApiConfig apiInfo) throws NoSuchMethodException {
		 this.registerMapping(apiInfo.getMethod(), apiInfo.getPath(), apiInfo.getScriptType());
	}
	public synchronized void registerMapping(String method,String path,String scriptType) throws NoSuchMethodException {
		if ("Code".equals(scriptType)) {
			return;
		}
		String pattern = path;
		if (StringUtils.isEmpty(pattern) || pattern.startsWith("TEMP-")) {
			return;
		}
		RequestMappingInfo mappingInfo = getRequestMappingInfo(pattern, method);
		if (mappingInfo != null) {
			return;
		}
		log.debug("Mapped [{}]{}", method, pattern);
		if(!StringUtils.isEmpty(method)) {
			mappingInfo = RequestMappingInfo.paths(pattern).methods(RequestMethod.valueOf(method)).build();
		}else {
			mappingInfo = RequestMappingInfo.paths(pattern).build();
		}
		Method targetMethod = RequestMappingExecutor.class.getDeclaredMethod("execute",HttpServletRequest.class, HttpServletResponse.class);
		requestMappingHandlerMapping.registerMapping(mappingInfo, mappingFactory, targetMethod);
	}

	/**
	 * 取消注册mapping
	 *
	 * @param apiInfo
	 */
	public synchronized void unregisterMappingForApiConfig(ApiConfig apiInfo) {
		this.unregisterMapping(apiInfo.getMethod(), apiInfo.getPath(), apiInfo.getScriptType());
	}
	public synchronized void unregisterMapping(String method,String path,String scriptType) {
		if ("Code".equals(scriptType)) {
			return;
		}
		String pattern = path;

		if (StringUtils.isEmpty(pattern) || pattern.startsWith("TEMP-")) {
			return;
		}
		RequestMappingInfo mappingInfo = getRequestMappingInfo(pattern, method);
		if (mappingInfo == null) {
			return;
		}
		log.info("Cancel Mapping [{}]{}", method==null?"":method, pattern);
		if(!StringUtils.isEmpty(method)) {
			mappingInfo = RequestMappingInfo.paths(pattern).methods(RequestMethod.valueOf(method)).build();
		}else {
			mappingInfo = RequestMappingInfo.paths(pattern).build();
		}
		requestMappingHandlerMapping.unregisterMapping(mappingInfo);
	}

	private RequestMappingInfo getRequestMappingInfo(String pattern, String method) {
		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		for (RequestMappingInfo info : map.keySet()) {
			Set<String> patterns = getPatterns(info);
			Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
			if (patterns.contains(pattern) && (methods.isEmpty() || methods.contains(RequestMethod.valueOf(method)))) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 判断是否是原始代码注册的mapping
	 * 
	 * @param method
	 * @param pattern
	 */
	public Boolean isCodeMapping(String pattern, String method) {
		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		for (RequestMappingInfo info : map.keySet()) {
			if (map.get(info).getMethod().getDeclaringClass() == RequestMappingExecutor.class) {
				continue;
			}
			Set<String> patterns = getPatterns(info);
			Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
			if (patterns.contains(pattern) && (methods.isEmpty() || methods.contains(RequestMethod.valueOf(method)))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(" RequestMappingService is init .... ");
	}

	public static Set<String> getPatterns(RequestMappingInfo info) {
		return info.getPatternsCondition() == null ? /* info.getPathPatternsCondition().getPatternValues() */null
				: info.getPatternsCondition().getPatterns();
	}

}
