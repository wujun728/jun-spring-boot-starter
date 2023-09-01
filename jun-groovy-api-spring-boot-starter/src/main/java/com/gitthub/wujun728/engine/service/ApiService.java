package com.gitthub.wujun728.engine.service;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson2.JSON;
import com.gitthub.wujun728.engine.common.model.ApiConfig;
import com.gitthub.wujun728.engine.common.model.ApiDataSource;
import com.gitthub.wujun728.engine.common.model.ApiSql;
import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jun.plugin.common.util.DbPoolManager;
import com.jun.plugin.common.util.RecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jun.plugin.common.util.DbPoolManager.master;

@Service
@Slf4j
public class ApiService {
	

	
	private String tablename = "api_config";
	
	@PostConstruct
	public void init(){
		DbPoolManager.initDefaultActiveRecordPlugin();
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
		Integer count = Db.use(master).queryInt("select count(*) from  "+tablename+"  ");
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<ApiDataSource> queryDatasourceList() {
		Page<Record> lists = Db.use(master).paginate(1,2,"select * "," from  "+tablename+"  where id <> ? ",1);
		//Console.log(JSON.toJSONString(lists));
		Console.log(JSON.toJSONString(RecordUtil.pageRecordToPage(lists)));

		String from = "from  "+tablename+"  where id > ?";
		String totalRowSql = "select count(*) " + from;
		String findSql = "select * " + from + " order by id";
		Db.paginateByFullSql(1, 10, totalRowSql, findSql, 18);

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> querySQLList(String apiId) {
		List<Record> lists = Db.use(master).find("select * from  "+tablename+"  ");
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
		Record record= Db.use(master).findById( tablename , id);
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
