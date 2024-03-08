package com.jun.plugin.groovy.cache;

import com.jun.plugin.groovy.common.model.ApiConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.util.ObjectUtils;

//import cn.hutool.core.lang.Assert;
//import cn.hutool.core.lang.Console;
//import cn.hutool.core.map.MapUtil;
//import cn.hutool.core.util.ObjectUtil;

@Slf4j
public class ApiConfigCache {

	/**
	 * 脚本列表
	 */
	private static ConcurrentMap<String, ApiConfig> groovyMap = new ConcurrentHashMap<>();

	/**
	 * 缓存 beanNameList脚本列表
	 */
	private static ConcurrentMap<String, String> beanNameMap = new ConcurrentHashMap<>();

	/**
	 * 把脚本缓存一下
	 *
	 * @param groovyList
	 */
	public static void put2map(List<ApiConfig> groovyList) {

		// 先清空
		if (!beanNameMap.isEmpty()) {
			beanNameMap.clear();
		}
		if (!groovyMap.isEmpty()) {
			groovyMap.clear();
		}
		for (ApiConfig groovyInfo : groovyList) {
			String scriptName = groovyInfo.getBeanName();
			if (!groovyMap.containsKey(scriptName)) {
				groovyMap.put(scriptName, groovyInfo);
			} else {
				// 发现重名groovy脚本
				log.warn("found duplication groovy script:" + groovyInfo);
			}
			// 缓存 beanNameList
			if (!beanNameMap.containsKey(groovyInfo.getPath())) {
				beanNameMap.put(groovyInfo.getPath(), groovyInfo.getBeanName());
			} else {
				log.warn("found duplication path:" + groovyInfo.getPath());
			}
		}
	}


	/**
	 * 更新map
	 *
	 * @param groovyInfos
	 */
	public static void update2map(List<ApiConfig>[] groovyInfos) {
		List<ApiConfig> addedApiConfigs = groovyInfos[0];
		List<ApiConfig> updatedApiConfigs = groovyInfos[1];
		List<ApiConfig> deletedApiConfigs = groovyInfos[2];
		addMap(addedApiConfigs);
		addMap(updatedApiConfigs);
		removeMap(deletedApiConfigs);
	}

	/**
	 * 新增
	 * @param groovyList
	 */
	private static void addMap(List<ApiConfig> groovyList) {
		for (ApiConfig groovyInfo : groovyList) {
			groovyMap.put(groovyInfo.getBeanName(), groovyInfo);
		}
	}

	/**
	 * 删除
	 * @param groovyList
	 */
	private static void removeMap(List<ApiConfig> groovyList) {
		for (ApiConfig groovyInfo : groovyList) {
			groovyMap.remove(groovyInfo.getBeanName());
		}
	}

	/**
	 * 根据名称获取脚本信息
	 * @param scriptName
	 * @return
	 */
	public static ApiConfig getByName(String scriptName) {
		return groovyMap.get(scriptName);
	}
	

	public static String getByPath(String path) {
		return beanNameMap.get(path);
	}
	
	public static ApiConfig getApiConfigByPath(String Path) {
		String beanName = beanNameMap.get(Path);
		if(ObjectUtils.isEmpty(beanName)) {
			log.error("beanname-{} 不能为空 ",beanNameMap.get(Path));
			log.error("Path-{} 没有注册的Bean ",Path);
		}
		ApiConfig info = groovyMap.get(beanNameMap.get(Path));
		if(ObjectUtils.isEmpty(info)) {
			log.error("Path-{} 不能为空 ",Path);
		}
		return info;
	}
 
	public static Map<String, ApiConfig> getApiConfigs() {
		return groovyMap;
	}
	
	public static Map<String, String> getBeanNameMap() {
		return beanNameMap;
	}
}
