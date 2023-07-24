package com.gitthub.wujun728.engine.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSONObject;
import com.gitthub.wujun728.engine.util.BeanMapUtils;
import com.gitthub.wujun728.engine.util.FieldUtils;
import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;

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
	
	public static String master = "_master";
	
	private String tablename = "api_config";
	
	@PostConstruct
	public void init(){
		String url = properties.getUrl();
		String username = properties.getUsername();
		String password = properties.getPassword();
		if(StringUtils.isEmpty(url)) {
			Console.log(SpringUtil.getProperty("project.datasource.url"));
			url = SpringUtil.getProperty("project.datasource.url");
			username = SpringUtil.getProperty("project.datasource.username");
			password = SpringUtil.getProperty("project.datasource.password");
		}
		Boolean isExtsis = false;
		try {
			DruidPlugin dp = new DruidPlugin(url, username, password);
			ActiveRecordPlugin arp = new ActiveRecordPlugin(master, dp);
			arp.setDevMode(true);
			arp.setShowSql(true);
			dp.start();
			arp.start();
			log.warn("Config have bean created by configName: {}",master);
			//Db.use(appNo);
		} catch (IllegalArgumentException e) {
			isExtsis = true;
			log.info(e.getMessage());
		}
		if( !isExtsis ){

		}
		/*DruidPlugin dp = new DruidPlugin(url, username, password);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(master, dp);
		// 与 jfinal web 环境唯一的不同是要手动调用一次相关插件的start()方法
		dp.start();
		arp.start();*/
		if(!StringUtils.isEmpty(properties.getApiconfig())) {
			tablename = properties.getApiconfig();
		}

	}

//	@Autowired
//	JdbcTemplate jdbcTemplate;// 不再直接依赖spring-jdbc了

	public Integer queryCountSql() {
		//Long aLong = jdbcTemplate.queryForObject("select count(*) from test ", Long.class);
		Integer count = Db.use(master).queryInt("select count(*) from test ");
		return count;
	}

	@Deprecated// 使用query方法替换find方法
	@SuppressWarnings("unchecked")
	public List<ApiConfig> queryApiConfigList() {

		List<Record> lists = Db.use(master).find("select * from "+tablename+" where status = 'ENABLE' ");
		//log.info(JSON.toJSONString(lists));
		// List<Map<String, Object>> lists = jdbcTemplate.queryForList("select * from  "+tablename+"  where status = 'ENABLE' ");
		List<ApiConfig> datas = convertRecord(lists,ApiConfig.class);
		if(!CollectionUtils.isEmpty(datas)) {
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
		//log.info(JSON.toJSONString(datas));
		return datas;
	}
	
	@SuppressWarnings("unchecked")
	public List<ApiDataSource> queryDatasourceList() {
		List<ApiDataSource> lists = Db.use(master).query("select * from api_datasource ");
		return lists;
	}
	
	@SuppressWarnings("unchecked")
	public List<ApiSql> querySQLList(String apiId) {
		List lists = Db.use(master).query("select * from api_sql where api_id = "+apiId);
		// List<Map<String, Object>> lists = jdbcTemplate.queryForList("select * from api_sql where api_id = "+apiId);
		List<ApiSql> datas = convert(lists,ApiSql.class);
		//log.info(JSON.toJSONString(datas));
		return datas;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List convertRecord(List<Record> lists,Class clazz){
		List datas = Lists.newArrayList();
		if(!CollectionUtils.isEmpty(lists)) {
			lists.forEach(item->{
				Object info = null;
				try {
					info = BeanMapUtils.columnsMapToBean(item.getColumns(), clazz);
				} catch (Exception e) {
					e.printStackTrace();
				}
				datas.add(info);
			});
			
		}
		return datas;
	}
	public Object convertRecord(Record record,Class clazz){
		Object info = null;
		try {
			info = BeanMapUtils.columnsMapToBean(record.getColumns(), clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public List convert(List<Map<String, Object>> lists,Class clazz){
		List datas = Lists.newArrayList();
		if(!CollectionUtils.isEmpty(lists)) {
			lists.forEach(item->{
				Map m = new HashMap<>();
				item.forEach((k,v)->{
					m.put(FieldUtils.columnNameToFieldName(String.valueOf(k)), v);
				});
				Object info = null;
				try {
					info = clazz.newInstance();
					info = JSONObject.parseObject(JSONObject.toJSONString(m), clazz);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				datas.add(info);
			});
			
		}
		//log.info(JSON.toJSONString(datas));
		return datas;
	}
	
	@SuppressWarnings("unchecked")
	public ApiDataSource getDatasource(String id) {
		ApiDataSource info = new ApiDataSource();
		Record record= Db.use(master).findById("api_datasource", id);
		//List<ApiDataSource> lists = jdbcTemplate.query("select * from api_datasource ",new BeanPropertyRowMapper(ApiDataSource.class));
		info = BeanMapUtils.columnsMapToBean(record.getColumns(), ApiDataSource.class);
		return info;
	}
	

}
