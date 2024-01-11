package com.jun.plugin.rest.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.jun.plugin.common.util.HttpRequestUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jun.plugin.common.Result;
import com.jun.plugin.common.exception.BusinessException;
import com.jun.plugin.common.util.FieldUtils;
import com.jun.plugin.common.db.RecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.jun.plugin.common.db.DataSourcePool.main;

@Slf4j
@org.springframework.web.bind.annotation.RestController
@RequestMapping({ "${platform.path:}/rest/{entityName}","${platform.path:}/public/rest/{entityName}" })
//@Api(value = "实体公共增删改查接口")
public class RestController {

	static AtomicReference<Map> tableCache = new AtomicReference<>();
	static AtomicReference<Map> primaryKeyCache = new AtomicReference<>();

	static {
		tableCache.set(Maps.newHashMap());
	}

	static Object getDefaultValue(String fieldname){
		if("createTime".equals(fieldname)){
			return DateUtil.now();
		}
		if("updateTime".equals(fieldname)){
			return DateUtil.now();
		}
		if("creator".equals(fieldname)){
			return "admin";
		}
		if("creator".equals(fieldname)){
			return "admin";
		}
		return null;
	}

	private static Result check(String tableName) {
		Map map = tableCache.get();
		if(!map.containsKey(tableName)){
			Table table = MetaUtil.getTableMeta(Db.use(main).getConfig().getDataSource(), tableName);
			map.put(tableName,table);
			tableCache.set(map);
			if(CollectionUtils.isEmpty(table.getColumns())){
				return Result.fail("实体对应的表不存在！");
			}
		}
		return null;
	}


	@GetMapping(path = "/list", produces = "application/json")
	//@ApiOperation(value = "返回实体数据列表", notes = "page与size同时大于零时返回分页实体数据列表,否则返回全部数据列表;
	public Result list(@PathVariable("entityName") String entityName,HttpServletRequest request) throws Exception {
		try {
			Map<String, Object>  parameters = HttpRequestUtil.getAllParameters(request);
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			String id = MapUtil.getStr(parameters,"id");
			String eq = MapUtil.getStr(parameters,"eq");
			String like = MapUtil.getStr(parameters,"like");
			Boolean isLayui = MapUtil.getBool(parameters,"layui",false);
			StringBuffer sql = new StringBuffer();
			sql.append("select * from "+ tableName +" ");
			if(StrUtil.isNotEmpty(eq)){
				eq = eq.replace(":","=").replace(",","AND");
				sql.append("WHERE ");
				sql.append("" + eq);
			}
//			Db.use().c
			List<Record> lists = Db.use(main).find(sql.toString());
			List<Map<String, Object>> datas = RecordUtil.recordToMaps(lists);
			return Result.success(datas);
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	@GetMapping(path = "/page", produces = "application/json")
	//@ApiOperation(value = "返回实体数据列表", notes = "page与size同时大于零时返回分页实体数据列表,否则返回全部数据列表;
	public Result page(@PathVariable("entityName") String entityName,HttpServletRequest request) throws Exception {
		try {
			Map<String, Object>  parameters = HttpRequestUtil.getAllParameters(request);
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			String id = MapUtil.getStr(parameters,"id");
			String like = MapUtil.getStr(parameters,"like");
			Integer page = MapUtil.getInt(parameters,"page");
			Integer limit = MapUtil.getInt(parameters,"limit");
			Boolean isLayui = MapUtil.getBool(parameters,"layui",false);
			if( (page==null || page ==0) || (limit==null || limit ==0) ){
				page = 1;
				limit = 10;
			}
			Page page1 = new Page<>();
			if(page !=null && limit != null ){
				String select = "select *";
				String from = " from "+ tableName;
				List<Map> likes = JSON.parseArray(like, Map.class);
				Page<Record> pages = Db.use(main).paginate(page, limit, select, from);
				Page datas = RecordUtil.pageRecordToPage(pages);
				page1 =  datas;
			}
			if(isLayui){
				return Result.success().put("count",page1.getTotalRow()).put("data",page1.getList()).put("limit",page1.getPageSize()).put("page",page1.getPageNumber());
			}else{
				return Result.success(page1);
			}
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}


	@RequestMapping(path = "/query", produces = "application/json")
	//@ApiOperation(value = "根据ID返回单个实体数据")
	public Result get(@PathVariable("entityName") String entityName,HttpServletRequest request) {
		try {
			Map<String, Object>  parameters = HttpRequestUtil.getAllParameters(request);
			String tableName = StrUtil.toUnderlineCase(entityName);
			String id = MapUtil.getStr(parameters,"id");
			if(StrUtil.isEmpty(id)){
				throw new BusinessException("接口必须参数id,可选参数primaryKey，有多列，均使用英文逗号分隔");
			}
			String primaryKey = MapUtil.getStr(parameters,"primaryKey");
			Result fail = check(tableName);
			if (fail != null) return fail;
			if(StrUtil.isEmpty(id)){
				throw new BusinessException("接口必须含参数id,可选参数primaryKey，有多列，均使用英文逗号分隔");
			}
			if(StrUtil.isEmpty(primaryKey)){
				primaryKey = "id";
			}

			if(StrUtil.isEmpty(primaryKey)){
				Record record= Db.use(main).findById( tableName , id);
				Map data =  RecordUtil.recordToMap(record);
				return Result.success(data);
			}else if(!primaryKey.contains(",")){
				Record record= Db.use(main).findByIds(tableName,primaryKey,id);
				Map data =  RecordUtil.recordToMap(record);
				return Result.success(data);
			}else if(primaryKey.contains(",")){
				Record record= Db.use(main).findByIds(tableName,primaryKey,id.split(","));
				Map data =  RecordUtil.recordToMap(record);
				return Result.success(data);
			}
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
		return Result.success();
	}

	@RequestMapping(path = "/delete", produces = "application/json")
	//@ApiOperation(value = "根据id删除实体数据" )
	public Result delete(@PathVariable("entityName") String entityName,HttpServletRequest request) {
		try {
			Map<String, Object>  parameters = HttpRequestUtil.getAllParameters(request);
			String tableName = StrUtil.toUnderlineCase(entityName);
			String id = MapUtil.getStr(parameters,"id");
			if(StrUtil.isEmpty(id)){
				throw new BusinessException("接口必须含参数id,可选参数primaryKey，有多列，均使用英文逗号分隔");
			}
			String primaryKey = MapUtil.getStr(parameters,"primaryKey");
			Result fail = check(tableName);
			if (fail != null) return fail;
			Boolean flag = false;
			if(StrUtil.isEmpty(id)){
				return Result.fail("删除主键id不能为空！");
			}
			if(StrUtil.isEmpty(primaryKey)){
				primaryKey = "id";
			}
			if(StrUtil.isEmpty(primaryKey)){
				if(id.contains(",")){
					String[] ids = id.split(",");
					Boolean flagTmp = true;
					for(String _id : ids){
						flagTmp = Db.use(main).deleteById(tableName,_id);
						if (flagTmp==false){
							flag = false;
						}else{
							flag = true;
						}
					}
				}else{
					flag = Db.use(main).deleteById(tableName,id);
				}
			}else if(StrUtil.isNotEmpty(primaryKey) && !primaryKey.contains(",")){
				flag = Db.use(main).deleteById(tableName,primaryKey,id);
			}else if(primaryKey.contains(",")){
				String[] ids = id.split(",");
				flag = Db.use(main).deleteByIds(tableName,primaryKey,ids);
			}
			if(flag){
				return Result.success("删除成功！");
			}else{
				return Result.fail("删除失败！");
			}
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			if(message.contains("Unknown column")){
				throw new BusinessException("接口必须参数id,可选参数primaryKey，其中primaryKey中的列在数据库不存在");
			}
			if(message.contains("number must equals id value number")){
				throw new BusinessException("接口必须参数id,可选参数primaryKey，有多列，均使用逗号分隔，当前参数个数与值的个数不一致");
			}
			log.error(message, e);
			return Result.error(message);
		}
	}


	@RequestMapping(path = "/save", produces = "application/json")
	//@ApiOperation(value = "新增实体数据", notes = "{\"name\":\"tom\",\"args\":1}")
	public Result create(@PathVariable("entityName") String entityName, HttpServletRequest request) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Map<String, Object> params = HttpRequestUtil.getAllParameters(request);
			Table talbe = MetaUtil.getTableMeta(Db.use(main).getConfig().getDataSource(),tableName);
			Collection<Column> columns =  talbe.getColumns();
			for(Column column : columns){
				if(!column.isNullable() && !column.isAutoIncrement()){
					if(MapUtil.getStr(params,column.getName())==null){
						throw new BusinessException("参数["+column.getName() + "]不能为空！");
					}
				}
			}
			Record record = new Record();
			for(Column column : columns){
				String val = MapUtil.getStr(params,column.getName());
				String fieldName = FieldUtils.columnNameToFieldName(column.getName());
				if(ObjectUtil.isEmpty(val)){
					val = MapUtil.getStr(params,fieldName);
				}
				val = getId(val);
				if(ObjectUtil.isNotEmpty(val)){
					record.set(column.getName(), (val) );
				}else {
					if(ObjectUtil.isNotEmpty(getDefaultValue(fieldName))){
						record.set(column.getName(), getDefaultValue(fieldName) );
					}
					//record.set(column.getName(), val ); //可为空字段，没传值默认新增不初始化
				}
				if(!column.isAutoIncrement()){

				}
			}
			Boolean resutl = false;
			resutl = Db.use(main).save(tableName, record);
			System.out.println("返回数据为：" + JSONUtil.toJsonStr(resutl));
			return Result.success(resutl);
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage().contains("Duplicate")){
				return Result.fail("数据重复，主键冲突："+e.getMessage());
			}
			if(e.getMessage().contains("Incorrect datetime")){
				return Result.fail("数据格式有误，日期格式不规范(yyyy-mm-dd)："+e.getMessage());
			}
			if(e.getMessage().contains("Data too long")){
				return Result.fail("数据字段值太长，超出最大长度："+e.getMessage());
			}
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	@RequestMapping(path = "/update", produces = "application/json")
	//@ApiOperation(value = "更新实体数据", notes = "不需要更新的字段不设置或设置为空,{\"name\":\"tom\",\"args\":1}")
	public Result update(@PathVariable("entityName") String entityName, HttpServletRequest request) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Map<String, Object> params = HttpRequestUtil.getAllParameters(request);
			Table talbe = MetaUtil.getTableMeta(Db.use(main).getConfig().getDataSource(),tableName);
			Collection<Column> columns =  talbe.getColumns();
			for(Column column : columns){
				if(!column.isNullable() /*&& !column.isAutoIncrement()*/){
					String fieldName = FieldUtils.columnNameToFieldName(column.getName());
					if(MapUtil.getStr(params,column.getName())==null && MapUtil.getStr(params,fieldName)==null){
						throw new BusinessException("参数["+fieldName + "]不能为空！");
					}
				}
			}
			Record record = new Record();
			for(Column column : columns){
				String val = MapUtil.getStr(params,column.getName());
				String fieldName = FieldUtils.columnNameToFieldName(column.getName());
				if(ObjectUtil.isEmpty(val)){
					val = MapUtil.getStr(params,fieldName);
				}
				val = getId(val);

				if(ObjectUtil.isNotEmpty(val)){
					record.set(column.getName(), (val) );
				}else {
					if(ObjectUtil.isNotEmpty(getDefaultValue(fieldName))){
						record.set(column.getName(), getDefaultValue(fieldName) );
					}
					//record.set(column.getName(), val ); //可为空字段，没传值默认不修改
				}
				if(!column.isAutoIncrement()){

				}
			}
			Boolean resutl = Db.use(main).update(tableName, record);
			return Result.success(resutl);
		} catch (Exception e1) {
			e1.printStackTrace();
			if(e1.getMessage().contains("Duplicate")){
				return Result.fail("数据重复，主键冲突："+e1.getMessage());
			}
			if(e1.getMessage().contains("Incorrect datetime")){
				return Result.fail("数据格式有误，日期格式不规范(yyyy-mm-dd)："+e1.getMessage());
			}
			if(e1.getMessage().contains("Data too long")){
				return Result.fail("数据字段值太长，超出最大长度："+e1.getMessage());
			}
			String message = ExceptionUtils.getMessage(e1);
			log.error(message, e1);
			return Result.error(message);
		}
	}

	protected String getId(String val) {
		//printMsg(val);
		if("{_objectId}".equalsIgnoreCase(val)){
			return IdUtil.objectId();
		}
		else if("{_simpleUUID}".equalsIgnoreCase(val)){
			return IdUtil.simpleUUID();
		}
		else if("{_snowflakeId}".equalsIgnoreCase(val)){
			return IdUtil.getSnowflakeNextIdStr();
		}
		else if("{_nanoId}".equalsIgnoreCase(val)){
			return IdUtil.nanoId();
		}else {
			return val;
		}
	}


}
