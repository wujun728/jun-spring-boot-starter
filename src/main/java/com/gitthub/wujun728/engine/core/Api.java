package com.gitthub.wujun728.engine.core;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import com.github.freakchick.orange.SqlMeta;
import com.github.freakchick.orange.engine.DynamicSqlEngine;
import com.gitthub.wujun728.engine.core.config.ApiPorperties;
import com.gitthub.wujun728.engine.core.entity.DataResult;
import com.gitthub.wujun728.engine.core.entity.DataSource;
import com.gitthub.wujun728.engine.core.entity.Sql;
import com.gitthub.wujun728.engine.core.util.JdbcUtil;
import com.gitthub.wujun728.engine.core.util.XmlParser;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Api {

    DynamicSqlEngine dynamicSqlEngine = new DynamicSqlEngine();

    ApiPorperties apiConfig;

    Map<String, Sql> sqlMap;
    Map<String, DataSource> dataSourceMap;
    
    public Api() {
    	this.dataSourceMap = Maps.newHashMap();
    	this.sqlMap = Maps.newHashMap();
    }

    public Api(ApiPorperties config) {
        this.apiConfig = config;

        try {
            File file = ResourceUtils.getFile(this.apiConfig.getSql());
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            this.sqlMap = XmlParser.parseSql(content);

            File dsFile = ResourceUtils.getFile(this.apiConfig.getDatasource());
            String dsText = FileUtils.readFileToString(dsFile, StandardCharsets.UTF_8);
            this.dataSourceMap = XmlParser.parseDatasource(dsText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void init(ApiPorperties config) {
        this.apiConfig = config;
        try {
            File file = ResourceUtils.getFile(this.apiConfig.getSql());
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            this.sqlMap = XmlParser.parseSql(content);

            File dsFile = ResourceUtils.getFile(this.apiConfig.getDatasource());
            String dsText = FileUtils.readFileToString(dsFile, StandardCharsets.UTF_8);
            this.dataSourceMap = XmlParser.parseDatasource(dsText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void initDataSource(DataSource config) {
        try {
            if(CollectionUtils.isEmpty(this.dataSourceMap)) {
            	this.dataSourceMap = Maps.newHashMap();
            }
            this.dataSourceMap.put(config.getId(), config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void initSqlMap(Sql sql) {
        try {
        	if(CollectionUtils.isEmpty(this.sqlMap)) {
            	this.sqlMap = Maps.newHashMap();
            }
            this.sqlMap.put(sql.getId(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataResult execute(Map<String, Object> data, String sqlId) {
        try {
            if (!sqlMap.containsKey(sqlId)) {
                return DataResult.fail("sql not found by id : " + sqlId);
            }
            Sql sql = this.sqlMap.get(sqlId);
            if (!dataSourceMap.containsKey(sql.getDatasourceId())) {
                return DataResult.fail("datasource not found : " + sql.getDatasourceId());
            }
            DataSource dataSource = dataSourceMap.get(sql.getDatasourceId());
            SqlMeta sqlMeta = dynamicSqlEngine.parse(sql.getText(), data);
            int isSelect = 0;
            if (sql.getType().equals("select")) {
                isSelect = 1;
            }

            DataResult result = JdbcUtil.executeSql(isSelect, dataSource, sqlMeta.getSql(), sqlMeta.getJdbcParamValues());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            return DataResult.fail(e.getMessage());
        }

    }

}
