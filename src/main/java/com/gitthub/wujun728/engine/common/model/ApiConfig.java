package com.gitthub.wujun728.engine.common.model;

import lombok.Data;

import java.util.List;

@Data
@Deprecated//TODO 后面精简模型，用不上的字段干掉，使用groovyInfo模型替换这个模型
public class ApiConfig {
	
    private Integer id;
    private Integer pid;
    private String path;
    private String name;
    private String method;
    private String params;
    private String beanName;
    private String datasourceId;
    private String scriptType;
    private String scriptContent;
    private String status;
    private Integer sort;
    private String extendInfo;
    private Integer openTrans;
    private String resutltParams;
    private String creator;
    private String createdTime;
    private String updateTime;
    private String lastUpdate;

    List<ApiSql> sqlList;
    String cachePlugin;
    String contentType;
}
