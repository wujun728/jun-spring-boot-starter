package com.jun.plugin.common.config;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.jun.plugin.common.db.DataSourcePool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.script.ScriptEngineManager;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author wujun
 * @date 2021/3/19
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.jun.plugin.common")
public class CommonAutoConfig implements ApplicationContextAware, InitializingBean {
	private ConfigurableApplicationContext applicationContext;
	private BeanDefinitionRegistry registry;
	private String packages[] = {"com.jun.plugin.common","com.jun.plugin.rest"};

	@Resource
	private DataSource dataSource;

	/**
	 * 以下五个的结果都是为spring容器为这个类创建Bean.
	 * @Bean 注解告诉Spring这个方法将会返回一个对象，这个对象要注册为Spring应用上下文中的bean。 通常方法体中包含了最终产生bean实例的逻辑
	 * @Component 注解表明一个类会作为组件类，并告知Spring要为这个类创建bean
	 * @return
	 */
	List<Class> annotationClasss = Arrays.asList(Configuration.class,/*Mapper.class,*/ Service.class, Component.class,
			Repository.class, Controller.class,ConfigurationProperties.class);
	@Override
	public void afterPropertiesSet() {
		initBeans();
		initDefaultDataSource();
		initActiveRecordPlugin();
	}

	private void initBeans() {
		for(String p : packages){
			annotationClasss.forEach(clazz->{
				Set<Class<?>> classes = ClassScanner.scanPackageByAnnotation(p, clazz);
				classes.forEach(c->{
					String beanName = StrUtil.lowerFirst(NamingCase.toCamelCase(c.getSimpleName()));
					if(!applicationContext.containsBean(beanName) && !applicationContext.containsBeanDefinition(beanName) ){
						registerBean(beanName,c);
					}
				});
			});
		}
		//applicationContext.refresh();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
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

	public DataSource initDefaultDataSource() {
		if(dataSource == null){
			dataSource = SpringUtil.getBean(DataSource.class);
			if(dataSource == null){
				DataSourcePool.initDefaultDataSource();
				if(dataSource == null){
					Console.log("initDefaultDataSource 数据源为空，需要手动初始化DataSource");
//					String url = SpringUtil.getProperty("spring.datasource.url");
//					String username = SpringUtil.getProperty("spring.datasource.username");
//					String password = SpringUtil.getProperty("spring.datasource.password");
//					String driver = SpringUtil.getProperty("spring.datasource.driver-class-name");
//					DataSource masterDataSource = DataSourcePool.init("master",url,username,password,driver);
//					DataSourcePool.initActiveRecordPlugin("master",masterDataSource);
				}else {
					log.info("datasource autowried init step2 ");
				}
			}else {
				log.info("datasource autowried init step1 ");
			}
		}else{
			log.info("datasource autowried init step0 ");
		}


		return dataSource;
	}
	public ActiveRecordPlugin initActiveRecordPlugin() {
		if(dataSource == null){
			dataSource = SpringUtil.getBean(DataSource.class);
		}
		if(dataSource != null){
			return DataSourcePool.initActiveRecordPlugin("main",dataSource);
		}else {
			return null;
		}
	}

}