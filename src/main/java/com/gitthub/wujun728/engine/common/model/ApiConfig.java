package com.gitthub.wujun728.engine.common.model;

import lombok.Data;

import java.util.List;

@Data
@Deprecated//TODO 后面精简模型，用不上的字段干掉，使用groovyInfo模型替换这个模型
public class ApiConfig {
	
	 /**
     * 主鍵ID
     */
    private Integer id;

    private String path;

    private String method;

    private String params;

    private String beanName;
    
//    private String interfaceId;

    private String datasourceId;
    
    private String scriptContent;

    private String scriptType;
    
    private String groupname;

    private String extendInfo;

    private String createdTime;
    
    private String modifiedTime;

    private Integer sort;
    
    private String creator;
    
    private String modifyname;
    
    

    String name;

    String note;


    List<ApiSql> sqlList;
 
//    Integer status;
//
//    Integer previlege;
//
//    String groupId;
//
    String cachePlugin;
//
//    String cachePluginParams;
//
//    String createTime;
//
//    String updateTime;
//
    String contentType;
//
    Integer openTrans;
//
//    String jsonParam;
//
//    String alarmPlugin;
//
//    String alarmPluginParam;
}
