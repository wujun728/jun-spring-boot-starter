package com.jun.plugin.common.config;

import com.jun.plugin.common.properties.ProjectProperties;
import com.jun.plugin.common.filter.XssFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * xss过滤拦截器
 * @author wujun
 * @date 2018/12/9
 */
@Configuration
public class XssFilterConfig {
    private static final int FILTER_ORDER = 1;

    @Bean
    @ConditionalOnMissingBean(FilterRegistrationBean.class)
    public FilterRegistrationBean xssFilterRegistrationBean(ProjectProperties properties) {
        ProjectProperties.Xxs propertiesXxs = properties.getXxs();
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<Filter>();
        registration.setFilter(new XssFilter());
        registration.setOrder(FILTER_ORDER);
        registration.setEnabled(propertiesXxs.isEnabled());
        registration.addUrlPatterns(propertiesXxs.getUrlPatterns().split(","));
        Map<String, String> initParameters = new HashMap<>(16);
        initParameters.put("excludes", propertiesXxs.getExcludes());
        registration.setInitParameters(initParameters);
        return registration;
    }
}
