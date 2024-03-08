package com.jun.plugin.common.config;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.StaticLog;
import com.jun.plugin.db.DataSourcePool;
import com.jun.plugin.db.record.Db;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngineManager;
import javax.sql.DataSource;
import java.util.*;

import static com.jun.plugin.db.DataSourcePool.main;

/**
 * @author wujun
 * @date 2021/3/19
 */
@Slf4j
@Configuration
public class CommonAutoConfig implements ApplicationContextAware, InitializingBean {
	private ConfigurableApplicationContext applicationContext;
	private BeanDefinitionRegistry registry;
	private String packages[] = {"com.jun.plugin.common","com.jun.plugin.rest"};

	private List<Class> annotationClasss = Arrays.asList(Configuration.class,/*Mapper.class,*/ Service.class, Component.class,
			Repository.class, Controller.class,ConfigurationProperties.class);

//	@Resource
//	private DataSource dataSource;


	@Override
	public void afterPropertiesSet() {
		initDefaultDataSource();
//		callRunners(applicationContext,new DefaultApplicationArguments(new String[]{""}));
//		initBeans();
//		initActiveRecordPlugin();
	}

	private void callRunners(ApplicationContext context, ApplicationArguments args) {
		//将实现ApplicationRunner和CommandLineRunner接口的类，存储到集合中
		List<Object> runners = new ArrayList<>();
		runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
		//按照加载先后顺序排序
		AnnotationAwareOrderComparator.sort(runners);
		for (Object runner : new LinkedHashSet<>(runners)) {
			if (runner instanceof CommandLineRunner) {
				// callRunner((CommandLineRunner) runner, args);
				try {
					//调用各个实现类中的逻辑实现
					((CommandLineRunner)runner).run(args.getSourceArgs());
				}
				catch (Exception ex) {
					throw new IllegalStateException("Failed to execute CommandLineRunner", ex);
				}
			}
		}
	}


	private void initBeans() {
		String url = SpringUtil.getProperty("project.config.packages");
		String [] pks =  packages;
		if(StringUtils.isNotEmpty(url)){
			pks =  ArrayUtil.addAll(packages,url.split(","));
		}
		for(String p : pks){
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

//	@Bean
//	@Lazy
//	@ConditionalOnMissingBean(DataSource.class)
//	public DataSource initDataSource() {
//		StaticLog.info("初始化jun-common的数据源1");
//		return initDefaultDataSource();
//	}

	public DataSource initDefaultDataSource() {
		DataSource dataSource = null;
			if(dataSource == null){
				dataSource = initDefaultDataSourceV1();
				if(dataSource == null){
					initActiveRecordPlusin();
					Console.log("initDefaultDataSource 数据源为空，需要手动初始化DataSource");
				}else {
					log.info("initDefaultDataSourceV1  datasource autowried init step2 ");
				}
			}else {
				log.info("datasource autowried sucess init step1 ");
			}
		return dataSource;
	}

	private static DataSource initActiveRecordPlusin() {
		String url = SpringUtil.getProperty("project.datasource.url");
		String username = SpringUtil.getProperty("project.datasource.username");
		String password = SpringUtil.getProperty("project.datasource.password");
		String driver = SpringUtil.getProperty("project.datasource.driver-class-name");
		StaticLog.info("project.datasource.url"+"="+url);
		StaticLog.info("project.datasource.username"+"="+username);
		StaticLog.info("project.datasource.password"+"="+password);
		StaticLog.info("project.datasource.driver-class-name"+"="+driver);
		StaticLog.info("current datasource is master ");
		DataSource masterDataSource = DataSourcePool.init("master",url,username,password,driver);
		Db.initAlias("master",url,username, password);
//		DataSourcePool.initActiveRecordPlugin("master",masterDataSource);
		return masterDataSource;
	}

	public static DataSource initDefaultDataSourceV1() {
		String url = SpringUtil.getProperty("spring.datasource.url");
		String username = SpringUtil.getProperty("spring.datasource.username");
		String password = SpringUtil.getProperty("spring.datasource.password");
		String driver = SpringUtil.getProperty("spring.datasource.driver-class-name");
		Console.log("initDefaultDataSource info  spring.datasource.url:{}",url);
		StaticLog.info("spring.datasource.url"+"="+url);
		StaticLog.info("spring.datasource.username"+"="+username);
		StaticLog.info("spring.datasource.password"+"="+password);
		StaticLog.info("spring.datasource.driver-class-name"+"="+driver);
		StaticLog.info("current datasource is default ");
		if(!StringUtils.isEmpty(url)) {
			Db.initAlias(main,url,username, password);
			return DataSourcePool.init(main,url,username,password,driver);
		}else {
			return null;
		}
	}

//	public ActiveRecordPlugin initActiveRecordPlugin() {
//		DataSource dataSource = null;
//		if(dataSource == null){
//			dataSource = SpringUtil.getBean(DataSource.class);
//		}
//		if(dataSource != null){
//			return DataSourcePool.initActiveRecordPlugin("main",dataSource);
//		}else {
//			return null;
//		}
//	}

}