package com.jun.plugin.groovy;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngineManager;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
@ComponentScan(basePackages = "com.jun.plugin.groovy")
public class ApiAutoConfig implements InitializingBean {
	private ConfigurableApplicationContext applicationContext;

	private BeanDefinitionRegistry registry;

	/**
	 * //以下五个的结果都是为spring容器为这个类创建Bean.
	 * @Bean 注解告诉Spring这个方法将会返回一个对象，这个对象要注册为Spring应用上下文中的bean。 通常方法体中包含了最终产生bean实例的逻辑
	 * @Component 注解表明一个类会作为组件类，并告知Spring要为这个类创建bean
	 */
	List<Class> annotationClasss = Arrays.asList(Configuration.class,Mapper.class, Service.class, Component.class, Repository.class, Controller.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		initBeans();
	}

	private void initBeans() {
		annotationClasss.forEach(clazz->{
			Set<Class<?>> mappers = ClassScanner.scanPackageByAnnotation("com.jun.plugin.groovy", clazz);
			mappers.forEach(c->{
				String beanName = NamingCase.toCamelCase(c.getSimpleName());
				if(!applicationContext.containsBean(beanName) && !applicationContext.containsBeanDefinition(beanName) ){
					registerBean(beanName,c);
				}
			});
		});
		applicationContext.refresh();
	}

	public void registerBean(String beanName, Class clazz) {
		this.registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		BeanDefinition beanDefinition = builder.getBeanDefinition();
		registry.registerBeanDefinition(beanName, beanDefinition);
	}



	@Bean
	@ConditionalOnMissingBean
	public ScriptEngineManager scriptEngineManager() {
		return new ScriptEngineManager();
	}

}