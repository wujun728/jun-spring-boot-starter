package com.gitthub.wujun728.engine.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSON;
import com.jun.plugin.common.properties.ApiProperties;
import com.gitthub.wujun728.engine.common.model.ApiConfig;
import com.gitthub.wujun728.engine.common.model.ApiDataSource;
import com.gitthub.wujun728.engine.common.model.ApiSql;
import com.gitthub.wujun728.engine.util.RecordUtil;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.spring.SpringUtil;
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.bean.copier.CopyOptions;
//import cn.hutool.db.Db;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiService {
	
	@Autowired
	ApiProperties properties;
	
	public static String master = "_main";
	
	private String tablename = "api_config";
	
	@PostConstruct
	public void init(){
		String url = properties.getUrl();
		String username = properties.getUsername();
		String password = properties.getPassword();
		Console.log("project.groovy-api.datasource.url:{}",url);
		if(StringUtils.isEmpty(url)) {
			Console.log("project.datasource.url:{}",SpringUtil.getProperty("project.datasource.url"));
			url = SpringUtil.getProperty("project.datasource.url");
			username = SpringUtil.getProperty("project.datasource.username");
			password = SpringUtil.getProperty("project.datasource.password");
		}
		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		try {
			//DruidPlugin dp = new DruidPlugin(url, username, password);
			ActiveRecordPlugin arp = new ActiveRecordPlugin(master, ds);
			arp.setDevMode(true);
			arp.setShowSql(true);
			//dp.start();
			arp.start();
			log.warn("Config have bean created by configName: {}",master);
		} catch (IllegalArgumentException e) {
			log.info(e.getMessage());
		}
		if(!StringUtils.isEmpty(properties.getApiconfig())) {
			tablename = properties.getApiconfig();
		}
	}
	@SuppressWarnings("unchecked")
	public List<ApiConfig> queryApiConfigList() {
		List<Record> lists = Db.use(master).find("select * from "+tablename+" where status = 'ENABLE' ");
		//log.info(JSON.toJSONString(lists));
		// List<Map<String, Object>> lists = jdbcTemplate.queryForList("select * from  "+tablename+"  where status = 'ENABLE' ");
		List<ApiConfig> datas = RecordUtil.recordToListBean(lists,ApiConfig.class);
		if(!CollectionUtils.isEmpty(datas)) {
			buildApiConifgSubApiSql(datas);
		}
		//log.info(JSON.toJSONString(datas));
		//test 111
		queryCountSql();
		queryDatasourceList();
		querySQLList("0");
		getDatasource("0");
		return datas;
	}


	public Integer queryCountSql() {
		//Long aLong = jdbcTemplate.queryForObject("select count(*) from test ", Long.class);
		Integer count = Db.use(master).queryInt("select count(*) from test ");
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<ApiDataSource> queryDatasourceList() {
		Page<Record> lists = Db.use(master).paginate(1,2,"select * "," from api_datasource where id <> ? ",1);
		//Console.log(JSON.toJSONString(lists));
		Console.log(JSON.toJSONString(RecordUtil.pageRecordToPage(lists)));

		String from = "from api_datasource where id > ?";
		String totalRowSql = "select count(*) " + from;
		String findSql = "select * " + from + " order by id";
		Db.paginateByFullSql(1, 10, totalRowSql, findSql, 18);

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> querySQLList(String apiId) {
		List<Record> lists = Db.use(master).find("select * from api_sql ");
		// List<Map<String, Object>> lists = jdbcTemplate.queryForList("select * from api_sql where api_id = "+apiId);
		List<ApiSql> datas = RecordUtil.recordToListBean(lists,ApiSql.class);
		List<Map<String, Object>>  datas2 = RecordUtil.recordToMaps(lists);
//		List<ApiSql> datas = RecordUtil.mapToBeans(lists,ApiSql.class);
		//log.info(JSON.toJSONString(datas));
		return datas2;
	}
	

	@SuppressWarnings("unchecked")
	public ApiDataSource getDatasource(String id) {
		ApiDataSource info = new ApiDataSource();
		Record record= Db.use(master).findById("api_datasource", id);
		//List<ApiDataSource> lists = jdbcTemplate.query("select * from api_datasource ",new BeanPropertyRowMapper(ApiDataSource.class));
		//info = BeanMapUtil.columnsMapToBean(record.getColumns(), ApiDataSource.class);
		return RecordUtil.recordToBean(record,ApiDataSource.class);
	}


	private static void buildApiConifgSubApiSql(List<ApiConfig> datas) {
		datas.stream().map(item->{
					List<ApiSql> sqlList = Lists.newArrayList();
					if("sql".equalsIgnoreCase(item.getScriptType())) {
						String sqls[] = item.getScriptContent().split(";");
						if(sqls.length>0) {
							for(String sql : sqls) {
								if(StringUtils.isEmpty(sql)) {
									continue;
								}
								ApiSql apisql = new ApiSql();
								apisql.setApiId(item.getId()+"");
								apisql.setSqlText(sql);
								sqlList.add(apisql);
							}
						}
					}
					item.setSqlList(sqlList);

					return item;
				}
		).collect(Collectors.toList());
	}
	

}
