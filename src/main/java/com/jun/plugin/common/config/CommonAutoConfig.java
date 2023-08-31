package com.jun.plugin.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author wujun
 * @date 2021/3/19
 */
@Configuration
@ComponentScan(basePackages = {"com.jun.plugin.common","com.jun.plugin.compile","com.jun.plugin.generator","com.jun.plugin.rest"})
public class CommonAutoConfig {

}
