package com.gitthub.wujun728.engine.common.model;

import lombok.Data;

@Data
@Deprecated//TODO 这个也干掉，合并到groovyInfo里面
public class ApiSql {

    Integer id;

    String apiId;

    String sqlText;

    String transformPlugin;

    String transformPluginParams;

}
