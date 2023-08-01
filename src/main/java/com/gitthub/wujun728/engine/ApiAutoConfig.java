package com.gitthub.wujun728.engine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngineManager;

@Configuration
@ComponentScan(basePackages = "com.gitthub.wujun728.engine")
public class ApiAutoConfig {

	@Bean
	@ConditionalOnMissingBean(ScriptEngineManager.class)
	public ScriptEngineManager scriptEngineManager() {
		return new ScriptEngineManager();
	}

}