package com.jun.plugin.groovy.service;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson2.JSON;
import com.jun.plugin.db.record.Db;
import com.jun.plugin.db.record.Page;
import com.jun.plugin.db.record.Record;
import com.jun.plugin.groovy.common.model.ApiConfig;
//import com.jun.plugin.groovy.common.model.ApiDataSource;
import com.jun.plugin.groovy.common.model.ApiDataSource;
import com.jun.plugin.groovy.common.model.ApiSql;
import com.google.common.collect.Lists;
import com.jun.plugin.db.record.RecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jun.plugin.db.DataSourcePool.main;

@Service
@Slf4j
public class ApiService {
	

	
	private String tablename = "api_config";
	
	@PostConstruct
	public void init(){
	}
	@SuppressWarnings("unchecked")
	public List<ApiConfig> queryApiConfigList() {
		List<Record> lists = Db.use(main).find("select * from "+tablename+" where status = 'ENABLE' ");
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
		Integer count = Db.use(main).queryInt("select count(*) from  "+tablename+"  ");
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<ApiDataSource> queryDatasourceList() {
		Page<Record> lists = Db.use(main).paginate(1,2,"select * "," from  "+tablename+"  where id <> ? ",1);
		//Console.log(JSON.toJSONString(lists));
		Console.log(JSON.toJSONString(RecordUtil.pageRecordToPage(lists)));

		String from = "from  "+tablename+"  where id > ?";
		String totalRowSql = "select count(*) " + from;
		String findSql = "select * " + from + " order by id";
		Db.paginateByFullSql(1, 10, totalRowSql, findSql, 18);
//		Db.paginate(1,10,findSql);

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map> querySQLList(String apiId) {
		List<Record> lists = Db.use(main).find("select * from  "+tablename+"  ");
		// List<Map<String, Object>> lists = jdbcTemplate.queryForList("select * from api_sql where api_id = "+apiId);
		List<ApiSql> datas = RecordUtil.recordToListBean(lists,ApiSql.class);
		List<Map>  datas2 = RecordUtil.recordToMaps(lists);
//		List<ApiSql> datas = RecordUtil.mapToBeans(lists,ApiSql.class);
		//log.info(JSON.toJSONString(datas));
		return datas2;
	}
	

	@SuppressWarnings("unchecked")
	public ApiDataSource getDatasource(String id) {
		ApiDataSource info = new ApiDataSource();
		Record record= Db.use(main).findById( tablename , id);
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
