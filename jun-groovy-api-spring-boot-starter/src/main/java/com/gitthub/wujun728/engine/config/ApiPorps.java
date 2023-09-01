package com.gitthub.wujun728.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.groovy-api.config")
public class ApiPorps {
    String sql;

    String datasource;


}
