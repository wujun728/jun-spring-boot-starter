package com.gitthub.wujun728.engine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gitthub.wujun728.engine.config.ApiPorperties;

import javax.script.ScriptEngineManager;

@Configuration
@ComponentScan(basePackages = "com.gitthub.wujun728.engine")
@EnableConfigurationProperties(ApiPorperties.class)
public class ApiAutoConfig {

	@Bean
	@ConditionalOnMissingBean(ScriptEngineManager.class)
	public ScriptEngineManager scriptEngineManager() {
		return new ScriptEngineManager();
	}

}