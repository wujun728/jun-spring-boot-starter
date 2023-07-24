package com.gitthub.wujun728.engine.groovy;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson2.JSON;
import com.gitthub.wujun728.engine.common.model.ApiConfig;
import com.gitthub.wujun728.engine.mapping.http.cache.IApiConfigCache;
import com.gitthub.wujun728.engine.service.ApiService;
import com.gitthub.wujun728.engine.groovy.cache.GroovyInfo;
import com.gitthub.wujun728.engine.groovy.cache.GroovyInnerCache;
import com.gitthub.wujun728.engine.mapping.http.RequestMappingService;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@Service
public class GroovyDynamicLoader implements ApplicationContextAware, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GroovyDynamicLoader.class);

	private Map<String,Class> classMap = new HashMap<>();

	private ConfigurableApplicationContext applicationContext;

	private BeanDefinitionRegistry registry;

	private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(
			GroovyDynamicLoader.class.getClassLoader());

	@Autowired
	private ApiService apiService;

	@Autowired
	private IApiConfigCache apiInfoCache;

	@Autowired
	private RequestMappingService requestMappingService;

	@Override
	public void afterPropertiesSet() throws Exception {

		long start = System.currentTimeMillis();
		System.out.println("开始解析groovy脚本...");

		logger.trace(" --- trace --- ");
		logger.debug(" --- debug --- ");
		logger.info(" --- info --- ");
		logger.warn(" --- warn --- ");
		logger.error(" --- error --- ");

		initNew();

		long cost = System.currentTimeMillis() - start;
		System.out.println("结束解析groovy脚本...，耗时：" + cost);
	}

	private void initNew() {
		try {
			apiService.init();

			List<ApiConfig> groovyScripts = apiService.queryApiConfigList();

			apiInfoCache.putAll(groovyScripts);

			List<GroovyInfo> groovyInfos = convert(groovyScripts);

			initNew(groovyInfos);

			refreshMapping(groovyScripts);
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
			Console.log("数据源配置有误："+e.getMessage());
			if(e.getMessage().contains("Config not found by configName")) {
				Console.log("jfinal数据源配置有误："+e.getMessage());
			}
		} catch (ActiveRecordException e) {
			//e.printStackTrace();
			if(e.getMessage().contains("doesn't exist")) {
				Console.log("api_config表在数据库不存在，配置有误："+e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initNew(List<GroovyInfo> groovyInfos) {
		if (CollectionUtils.isEmpty(groovyInfos)) {
			return;
		}
		this.registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
		for (GroovyInfo groovyInfo : groovyInfos) {
			try {
				Class clazz = groovyClassLoader.parseClass(groovyInfo.getGroovyContent());
				BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
				BeanDefinition beanDefinition = builder.getBeanDefinition();
				registry.registerBeanDefinition(groovyInfo.getBeanName(), beanDefinition);
			} catch (BeanDefinitionStoreException e) {
				log.error("当前Groovy脚本执行失败："+JSON.toJSONString(groovyInfo));
				e.printStackTrace();
			} catch (CompilationFailedException e) {
				log.error("当前Groovy脚本执行失败："+JSON.toJSONString(groovyInfo));
				e.printStackTrace();
			}catch (Exception e) {
				log.error("当前Groovy脚本执行失败："+JSON.toJSONString(groovyInfo));
			}
			log.info("当前groovyInfo加载完成,className-{},path-{},beanName-{},BeanType-{}：",groovyInfo.getClassName(),groovyInfo.getPath(),groovyInfo.getBeanName(),groovyInfo.getBeanType());
		}
		GroovyInnerCache.put2map(groovyInfos);
	}


	public void refreshNew() {

		List<ApiConfig> groovyScripts = apiService.queryApiConfigList();

		apiInfoCache.putAll(groovyScripts);

		List<GroovyInfo> groovyInfos = convert(groovyScripts);

		if (CollectionUtils.isEmpty(groovyInfos)) {
			return;
		}

		destroyBeanDefinition(groovyInfos);

		//destroyScriptBeanFactory();
		initNew(groovyInfos);

		GroovyInnerCache.put2map(groovyInfos);

		refreshMapping(groovyScripts);
	}
	
	private List<GroovyInfo> convert(List<ApiConfig> list) {

		List<GroovyInfo> groovyInfos = new LinkedList<>();

		if (CollectionUtils.isEmpty(list)) {
			return groovyInfos;
		}

		for (ApiConfig item : list) {
			if (!"Class".equals(item.getScriptType())) {
				continue;
			}
			GroovyInfo groovyInfo = new GroovyInfo();
			groovyInfo.setClassName(item.getBeanName());
			groovyInfo.setGroovyContent(item.getScriptContent());
			groovyInfo.setPath(item.getPath());
			groovyInfo.setBeanName(item.getBeanName());
			groovyInfo.setBeanType(item.getScriptType());
			groovyInfos.add(groovyInfo);

		}

		return groovyInfos;
	}

	private void destroyBeanDefinition(List<GroovyInfo> groovyInfos) {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
				.getAutowireCapableBeanFactory();
		for (GroovyInfo groovyInfo : groovyInfos) {
			try {
				beanFactory.removeBeanDefinition(groovyInfo.getClassName());
			} catch (Exception e) {
				System.out
						.println("【Groovy】 delete groovy bean definition exception. skip:" + groovyInfo.getClassName());
			}
		}
	}

//	private void destroyScriptBeanFactory() {
//		String[] postProcessorNames = applicationContext.getBeanFactory()
//				.getBeanNamesForType(CustomScriptFactoryPostProcessor.class, true, false);
//		for (String postProcessorName : postProcessorNames) {
//			CustomScriptFactoryPostProcessor processor = (CustomScriptFactoryPostProcessor) applicationContext
//					.getBean(postProcessorName);
//			processor.destroy();
//		}
//	}

	/**
	 * 重建单一请求的注册与缓存
	 */
	public void refreshMapping(List<ApiConfig> groovyScripts) {
		try {
			for (ApiConfig apiInfo : groovyScripts) {
				// 取消历史注册
				if (apiInfo != null) {
					requestMappingService.unregisterMappingForApiConfig(apiInfo);
					apiInfoCache.remove(apiInfo);
				}

				// 重新注册mapping
				if (apiInfo != null) {
					requestMappingService.registerMappingForApiConfig(apiInfo);
					apiInfoCache.put(apiInfo);
				}
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}
}
