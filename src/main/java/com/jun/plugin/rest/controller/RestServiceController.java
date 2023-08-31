package com.jun.plugin.rest.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import cn.hutool.json.JSONUtil;
import com.gitthub.wujun728.engine.util.HttpRequestLocal;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jun.plugin.common.Result;
import com.jun.plugin.common.exception.BusinessException;
import com.jun.plugin.common.util.FieldUtils;
import com.jun.plugin.common.util.RecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jun.plugin.common.util.DbPoolManager.master;

@Slf4j
@RestController
@RequestMapping({ "${platform.path:}/rest/{entityName}","${platform.path:}/public/rest/{entityName}" })
//@Api(value = "实体公共增删改查接口")
public class RestServiceController {


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


	@GetMapping(path = "/page", produces = "application/json")
	//@ApiOperation(value = "返回实体数据列表", notes = "page与size同时大于零时返回分页实体数据列表,否则返回全部数据列表;
	public Result page(@PathVariable("entityName") String entityName, Integer size, Integer page, String export,
			@RequestParam Map<String, String> queryParams) throws Exception {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			return Result.success(queryPage(size,page,tableName));
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	@GetMapping(path = "/layui/page", produces = "application/json")
	//@ApiOperation(value = "返回实体数据列表", notes = "page与size同时大于零时返回分页实体数据列表,否则返回全部数据列表;
	public Result layuiList(@PathVariable("entityName") String entityName, Integer size, Integer page, String export,
							@RequestParam Map<String, String> queryParams) throws Exception {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Page page1 = queryPage(size,page,tableName);
			return Result.success().put("count",page1.getTotalRow()).put("data",page1.getList()).put("limit",page1.getPageSize()).put("page",page1.getPageNumber());
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	@GetMapping(path = "/list", produces = "application/json")
	//@ApiOperation(value = "返回实体数据列表", notes = "page与size同时大于零时返回分页实体数据列表,否则返回全部数据列表;
	public Result list(@PathVariable("entityName") String entityName, Integer size, Integer page, String export,
			@RequestParam Map<String, String> queryParams) throws Exception {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			return Result.success(queryList(size,page,tableName));
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	private static Page queryPage(Integer size, Integer page, String tableName) {
		if(page !=null && size != null ){
			Page<Record> pages = Db.use(master).paginate(page, size,"select *"," from "+ tableName);
			Page datas = RecordUtil.pageRecordToPage(pages);
			return datas;
		}
		return null;
	}
	private static List queryList(Integer size, Integer page, String tableName) {
		List<Record> lists = Db.use(master).find("select * from "+ tableName +" ");
		List<Map<String, Object>> datas = RecordUtil.recordToMaps(lists);
		return datas;
	}

	private static Result check(String tableName) {
		Table table = MetaUtil.getTableMeta(Db.use(master).getConfig().getDataSource(), tableName);
		if(CollectionUtils.isEmpty(table.getColumns())){
			return Result.fail("实体对应的表不存在！");
		}
		return null;
	}

	@GetMapping(path = "/findById", produces = "application/json")
	//@ApiOperation(value = "根据ID返回单个实体数据")
	public Result get(@PathVariable("entityName") String entityName, String id, String primaryKey) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			if(StrUtil.isEmpty(primaryKey)){
				Record record= Db.use(master).findById( tableName , id);
				Map data =  RecordUtil.recordToMap(record);
				return Result.success(data);
			}else if(!primaryKey.contains(",")){
				Record record= Db.use(master).findByIds(tableName,primaryKey,id);
				Map data =  RecordUtil.recordToMap(record);
				return Result.success(data);
			}else if(primaryKey.contains(",")){
				Record record= Db.use(master).findByIds(tableName,primaryKey,id.split(","));
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

	@PostMapping(path = "/save", produces = "application/json")
	//@ApiOperation(value = "新增实体数据", notes = "{\"name\":\"tom\",\"args\":1}")
	public Result create(@PathVariable("entityName") String entityName, HttpServletRequest request) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Map<String, Object> params = HttpRequestLocal.getAllParameters(request);
			Table talbe = MetaUtil.getTableMeta(Db.use(master).getConfig().getDataSource(),tableName);
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
				if(StrUtil.isEmpty(val)){
					val = MapUtil.getStr(params,fieldName);
				}
				val = getId(val);
				if(StrUtil.isNotEmpty(val)){
					record.set(column.getName(), (val) );
				}else {
					record.set(column.getName(), getDefaultValue(fieldName) );
				}
				if(!column.isAutoIncrement()){

				}
			}
			Boolean resutl = false;
			resutl = Db.use(master).save(tableName, record);
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

	@PutMapping(path = "/update", produces = "application/json")
	//@ApiOperation(value = "更新实体数据", notes = "不需要更新的字段不设置或设置为空,{\"name\":\"tom\",\"args\":1}")
	public Result update(@PathVariable("entityName") String entityName, HttpServletRequest request) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Map<String, Object> params = HttpRequestLocal.getAllParameters(request);
			Table talbe = MetaUtil.getTableMeta(Db.use(master).getConfig().getDataSource(),tableName);
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
				if(StrUtil.isEmpty(val)){
					val = MapUtil.getStr(params,fieldName);
				}
				val = getId(val);

				if(StrUtil.isNotEmpty(val)){
					record.set(column.getName(), (val) );
				}else {
					record.set(column.getName(), getDefaultValue(fieldName) );
				}
				if(!column.isAutoIncrement()){

				}
			}
			Boolean resutl = Db.use(master).update(tableName, record);
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

	@DeleteMapping(path = "/delete", produces = "application/json")
	//@ApiOperation(value = "根据id删除实体数据" )
	public Result delete(@PathVariable("entityName") String entityName, String id, String primaryKey) {
		try {
			String tableName = StrUtil.toUnderlineCase(entityName);
			Result fail = check(tableName);
			if (fail != null) return fail;
			Boolean flag = false;
			if(StrUtil.isEmpty(primaryKey)){
				flag = Db.use(master).deleteById(tableName,id);
			}else if(StrUtil.isNotEmpty(primaryKey) && !primaryKey.contains(",")){
				flag = Db.use(master).deleteById(tableName,primaryKey,id);
			}else if(primaryKey.contains(",")){
				flag = Db.use(master).deleteById(tableName,primaryKey,id.split(","));
			}
			if(flag){
				return Result.success("删除成功！");
			}else{
				return Result.fail("删除失败！");
			}
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			log.error(message, e);
			return Result.error(message);
		}
	}

	//
//	@GetMapping(path = "/enum/{fieldName}/list", produces = "application/json")
//	public Result enumList(@PathVariable("entityName") String entityName, @PathVariable("fieldName") String fieldName)
//			throws Exception {
//		try {
//			Result result = check(entityName, "GET");
//			if (!result.isSuccess())
//				return result;
//			return restService.enumList(entityName, fieldName);
//		} catch (Exception e) {
//			String message = ExceptionUtils.getMappingMessage(e);
//			LogUtils.error(message, e);
//			return Result.error(message);
//		}
//	}
//
//	@GetMapping(path = "/fields", produces = "application/json")
//	public Result fields(@PathVariable("entityName") String entityName) {
//		try {
//			Result result = check(entityName, "GET");
//			if (!result.isSuccess())
//				return result;
//			return restService.fields(entityName);
//		} catch (Exception e) {
//			String message = ExceptionUtils.getMappingMessage(e);
//			LogUtils.error(message, e);
//			return Result.error(message);
//		}
//	}

}
