package com.jun.plugin.common.generator;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import cn.hutool.log.StaticLog;
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.jun.plugin.common.util.PropertiesUtil;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.*;
import java.util.*;


/**
 * 代码生成器 工具类
 *
 * @author wujun
 * @version V1.0
 * @date 2020年3月18日
 */
@Slf4j
public class GenUtils {

	private static final Logger logger = LoggerFactory.getLogger(GenUtils.class);

	private static DataSource dataSource;
//	public static Properties props; // 配置文件
	public static Map props; // 配置文件
	public static List<String> templates; // 模板文件f
	public static List<String> filePaths; // 生成文件名

    public static void init() {
		if(CollectionUtils.isEmpty(props)){
			PropertiesUtil.loadProps("config-v1.properties");
			props = new HashMap<>();
			props.putAll(PropertiesUtil.getAllProperty());
		}
	}
    public static String getProp(String key) {
		String val = MapUtil.getStr(props,key);
		return val;
	}
    public static List<String> getFilePaths(List<String> templates, ClassInfo classInfo) {
        List<String> filePaths = new ArrayList<>();
		for(String template : templates){
			String path_tmep = FileNameUtil.getName(template).replace(".ftl","");
			String filename_resc = path_tmep;
			//String package3Path = String.format("/%s/", path1.contains(".") ? path1.replaceAll("\\.", "/") : path1);
			if(template.contains(".java")){
				String path1 = path_tmep.substring(0,path_tmep.lastIndexOf("."));
				String filename_tmep = upperCaseFirstWordV2(path1);
				String packageName = getProp("packageName")+"."+path1;
				String package2Path = String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
				filePaths.add(getProp("project_path") +File.separator+ getProp("javaPath") + package2Path + classInfo.getClassName() + filename_tmep + ".java");
			}else{
				filePaths.add(getProp("project_path") +File.separator+ getProp("resourcesPath") + File.separator+classInfo.getClassName() +File.separator+ filename_resc );
			}
		}
        return filePaths;
    }

//	public static void getFile(String path, List<Map<String, Object>> list) {
//		File file = new File(path);
//		File[] array = file.listFiles();
//		for (int i = 0; i < array.length; i++) {
//			if (array[i].isFile()) {
//				Map<String, Object> map = new HashMap<String, Object>();
//				// only take file name
//				// System.out.println("^^^^^" + array[i].getName());
//				// take file path and name
//				// System.out.println("*****" + array[i].getPath());
//				map.put(array[i].getName(), array[i].getPath());
//				list.add(map);
//			} else if (array[i].isDirectory()) {
//				getFile(array[i].getPath(), list);
//			}
//		}
//	}

	public static String replace_(String str) {
		// 根据下划线分割
		String[] split = str.split("_");
		// 循环组装
		String result = split[0];
		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				result += firstUpper(split[i]);
			}
		}
		return result;
	}

	public static String firstUpper(String str) {
		// log.info("str:"+str+",str.length="+str.length());
		if (!org.springframework.util.StringUtils.isEmpty(str)) {
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		} else {
			return str;
		}
	}

//	public static String firstLower(String str) {
//		return str.substring(0, 1).toLowerCase() + str.substring(1);
//	}

	public static String replaceTabblePreStr(String str) {
//		str = str.toLowerCase().replaceFirst("tab_", "").replaceFirst("tb_", "").replaceFirst("t_", "");
		for (String x : getProp("tableRemovePrefixes").split(",")) {
			if(str.startsWith(x.toLowerCase())){
				str = str.replaceFirst(x.toLowerCase(), "");
			}
		}
		return str;
	}

	public static String replaceRowPreStr(String str) {
//		str = str.toLowerCase().replaceFirst("tab_", "").replaceFirst("tb_", "").replaceFirst("t_", "");
		for (String x : getProp("rowRemovePrefixes").split(",")) {
			str = str.replaceFirst(x.toLowerCase(), "");
		}
		return str;
	}

	public static String simpleName(String type) {
		return type.replace("java.lang.", "").replaceFirst("java.util.", "");
	}

//	public static String upperCaseFirstWord(String str) {
//		return str.substring(0, 1).toUpperCase() + str.substring(1);
//	}
	public static String upperCaseFirstWordV2(String str) {
		if(str!=null && str.length()>0){
			if(str.contains(".")){
				String strs[] = str.split("\\.");
				String temp = "";
				for(String strtmp : strs){
					temp += strtmp.substring(0, 1).toUpperCase() + strtmp.substring(1);
				}
				return temp;
			}else{
				return str.substring(0, 1).toUpperCase() + str.substring(1);
			}
		}
		return str;
	}

	public static String getType(int value) {
		switch (value) {
		case -6:
			return "java.lang.Integer";
		case 5:
			return "java.lang.Integer";
		case 4:
			return "java.lang.Integer";
		case -5:
			return "java.lang.Long";
		case 6:
			return "java.lang.Float";
		case 8:
			return "java.lang.Double";
		case 1:
			return "java.lang.String";
		case 12:
			return "java.lang.String";
		case -1:
			return "java.lang.String";
		case 91:
			return "java.util.Date";
		case 92:
			return "java.util.Date";
		case 93:
			return "java.util.Date";
		case 16:
			return "java.lang.Boolean";
		default:
			return "java.lang.String";
		}
	}
	
	
	public static void processTemplatesFileWriter(ClassInfo classInfo, Map<String, Object> datas, List<String> templates) throws IOException, TemplateException {
		for(int i = 0 ; i < templates.size() ; i++) {
			if(CollectionUtils.isEmpty(filePaths)){
				GenUtils.processFile(templates.get(i), datas, GenUtils.getFilePaths(templates,classInfo).get(i));
			}else{
				GenUtils.processFile(templates.get(i), datas, filePaths.get(i));
			}
		}
	}

	public static void processFile(String templateName, Map<String, Object> data, String filePath)
			throws IOException, TemplateException {
		Template template = getConfiguration().getTemplate(templateName);
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		template.process(data, new FileWriter(file));
		System.out.println(filePath + " 生成成功");
	}

	/***
	 * 模板构建，StringWriter 返回构建后的文本，不生成文件
	 */
	public static String processString(String templateName, Map<String, Object> params)
			throws IOException, TemplateException {
		Template template = getConfiguration().getTemplate(templateName);
		StringWriter result = new StringWriter();
		template.process(params, result);
		String htmlText = result.toString();
		return htmlText;
	}

	@Deprecated
	public static String genTemplateStr(Map<String, Object> params,String templateContent)
			throws IOException, TemplateException {
		return genTemplateStr(params,"temp",templateContent);
	}
	/**
	 *  解析模板
	 * @param params 内容
	 * @param templateName 参数
	 * @param templateContent 参数
	 * @return
	 */
	public static String genTemplateStr(Map<String, Object> params,String templateName,String templateContent)
			throws IOException, TemplateException {
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		Template template = new Template(templateName, new StringReader(templateContent));
		StringWriter result = new StringWriter();
		template.process(params, result);
		String htmlText = result.toString();
		return htmlText;
	}

	private static freemarker.template.Configuration getConfiguration() throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(
				freemarker.template.Configuration.VERSION_2_3_23);
		if(getProp("userDefaultTemplate").equalsIgnoreCase("false")){
			cfg.setDirectoryForTemplateLoading(new File(getProp("project_path") +File.separator+  getProp("template_path")));
		}else{
			cfg.setClassForTemplateLoading(GenUtils.class,"/");
		}
		cfg.setDefaultEncoding("UTF-8");
		cfg.setNumberFormat("#");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
		return cfg;
	}


	/***
	 * 模板构建，输出源码字符串
	 */
	public static Map<String, String> processTemplatesStringWriter(Map<String, Object> datas, List<String> templates)
			throws IOException, TemplateException {
		Map<String, String> result = new HashMap<String, String>();
		for(int i = 0 ; i < templates.size() ; i++) {
			GenUtils.processString(templates.get(i), datas );
			result.put(templates.get(i), GenUtils.processString(templates.get(i), datas));
		}
		return result;
	}


	public static ClassInfo getClassInfo(Table table) {
		// V1 初始化数据及对象 模板V1 field List
		List<FieldInfo> fieldList = new ArrayList<FieldInfo>();
		for (Column column : table.getColumns()) {
			// V1 初始化数据及对象
			String remarks = column.getComment();// cloumnsSet.getString("REMARKS");// 列的描述
			String columnName = column.getName();// cloumnsSet.getString("COLUMN_NAME"); // 获取列名
			String javaType = GenUtils.getType(column.getType()/*cloumnsSet.getInt("DATA_TYPE")*/);// 获取类型，并转成JavaType
			String columnType = column.getTypeName();// 获取类型，并转成JavaType
			long COLUMN_SIZE = column.getSize();// cloumnsSet.getInt("COLUMN_SIZE");// 获取
			String COLUMN_DEF = column.getColumnDef();// cloumnsSet.getString("COLUMN_DEF");// 获取
			Boolean nullable = column.isNullable();// cloumnsSet.getInt("NULLABLE");// 获取
			String propertyName = GenUtils.replace_(GenUtils.replaceRowPreStr(columnName));// 处理列名，驼峰
			Boolean isPk = column.isPk();

			// V1 初始化数据及对象
			FieldInfo fieldInfo = new FieldInfo();
			fieldInfo.setColumnName(columnName);
			fieldInfo.setColumnType(columnType);
			fieldInfo.setFieldName(propertyName);
			fieldInfo.setFieldClass(GenUtils.simpleName(javaType));
			fieldInfo.setFieldComment(remarks);
			fieldInfo.setColumnSize(COLUMN_SIZE);
			fieldInfo.setNullable(nullable);
			fieldInfo.setFieldType(javaType);
			fieldInfo.setIsPrimaryKey(isPk);
			fieldList.add(fieldInfo);
		}
		if (fieldList != null && fieldList.size() > 0) {
			ClassInfo classInfo = new ClassInfo();
			classInfo.setTableName(table.getTableName());
			String className = GenUtils.replace_(GenUtils.replaceTabblePreStr(table.getTableName())); // 名字操作,去掉tab_,tb_，去掉_并转驼峰
			String classNameFirstUpper = GenUtils.firstUpper(className); // 大写对象
			classInfo.setClassName(classNameFirstUpper);
			if(table.getComment().contains("表")){
				classInfo.setClassComment(table.getComment().replace("表",""));
			}else{
				classInfo.setClassComment(table.getComment());
			}
			classInfo.setFieldList(fieldList);
			classInfo.setPkSize(table.getPkNames().size());
			return classInfo;
		}
		return null;
	}


	public static void genTables(List<ClassInfo> classInfos, List<String> templates ) throws Exception {

		if(CollectionUtils.isEmpty(templates)){
			templates = GenUtils.templates;
			if(CollectionUtils.isEmpty(templates)){
				templates = GenUtils.templates;
				logger.error("代码生成模板未初始化，请初始化【templates】");
			}
		}
		List<String> finalTemplates = templates;
		classInfos.forEach(classInfo -> {
			Map<String, Object> datas = new HashMap<String, Object>();
			datas.put("classInfo", classInfo);
			props.forEach((k, v)->{
				if(String.valueOf(v).equalsIgnoreCase("true")||String.valueOf(v).equalsIgnoreCase("false")){
					datas.put(String.valueOf(k), Boolean.valueOf(String.valueOf(v)));
				}else{
					datas.put(String.valueOf(k), v);
				}
			});
			datas.put("package", getProp("package"));
			datas.put("author", getProp("author"));
			datas.put("email", getProp("email"));
			datas.put("datetime", DateUtil.now());
			datas.put("identity", IdUtil.getSnowflakeNextIdStr());
			datas.put("addId", IdUtil.getSnowflakeNextIdStr());
			datas.put("updateId", IdUtil.getSnowflakeNextIdStr());
			datas.put("deleteId", IdUtil.getSnowflakeNextIdStr());
			datas.put("selectId", IdUtil.getSnowflakeNextIdStr());

			Map<String, String> result = new HashMap<String, String>();
			try {
				result = GenUtils.processTemplatesStringWriter(datas, finalTemplates);
				GenUtils.processTemplatesFileWriter(classInfo, datas, finalTemplates);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TemplateException e) {
				e.printStackTrace();
			}
			// 计算,生成代码行数
			int lineNum = 0;
			for (Map.Entry<String, String> item : result.entrySet()) {
				if (item.getValue() != null) {
					lineNum += StringUtils.countMatches(item.getValue(), "\n");
				}
			}
			logger.info("生成代码行数：{}", lineNum);
		});
		if (CollectionUtils.isEmpty(classInfos)) {
			logger.error("找不到当前表的元数据classInfos.size()：{}", classInfos.size());
		}
	}



	public static List<ClassInfo> getClassInfos(List<String> tableNames) {
		DataSource ds = getDruidDataSource();
		List<ClassInfo> classInfos = Lists.newArrayList();
		tableNames.stream().forEach(t -> {
			Table table = MetaUtil.getTableMeta(ds, t);
			if(table.getPkNames().size()>0){//没有主键是不生成的
				classInfos.add(GenUtils.getClassInfo(table));
			}else{
				StaticLog.error("表"+table.getTableName()+"没有主键是不生成代码的，至少得一个主键");
			}
		});
		return classInfos;
	}


	private static DataSource getDruidDataSource() {
		if(dataSource == null){
			DruidDataSource ds = new DruidDataSource();
			ds.setDriverClassName(getProp("jdbc.driver"));
			ds.setUrl(getProp("jdbc.url"));
			ds.setUsername(getProp("jdbc.username"));
			ds.setPassword(getProp("jdbc.password"));
			ds.setRemoveAbandoned(true);
			ds.setRemoveAbandonedTimeout(600);
			ds.setLogAbandoned(true);
//			ds.setBreakAfterAcquireFailure(true);
			ds.setTimeBetweenConnectErrorMillis(60000);//1分钟
//			ds.setConnectionErrorRetryAttempts(0);
			ds.setValidationQuery("SELECT 1");//用来检测连接是否有效
//			ds.setTestOnBorrow(false);//借用连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
//			ds.setTestOnReturn(false);//归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
			//连接空闲时检测，如果连接空闲时间大于timeBetweenEvictionRunsMillis指定的毫秒，执行validationQuery指定的SQL来检测连接是否有效
//			ds.setTestWhileIdle(true);//如果检测失败，则连接将被从池中去除
			ds.setMaxActive(200);
			ds.setInitialSize(5);
			dataSource = ds;
			return ds;
		}
		return dataSource;
	}




	public static DataSource getDataSource() {
		return dataSource;
	}

	public static void setDataSource(DataSource dataSource) {
		GenUtils.dataSource = dataSource;
	}

	public static List<String> getTemplates() {
		return templates;
	}

	public static void setTemplates(List<String> templates) {
		GenUtils.templates = templates;
	}

	public static Map getProps() {
		return props;
	}

	public static void putProps(Map props) {
		init();
		GenUtils.props.putAll(props);
	}

	public static void genCode(String tableNames) throws Exception {
		init();
		if( CollectionUtils.isEmpty(templates)){
			props.put("userDefaultTemplate","true");// 未设置模板，使用内置模板
		}
		if(getProp("userDefaultTemplate").equalsIgnoreCase("true")  ){
			templates = Lists.newArrayList();
			// ************************************************************************************
			templates.add("/templates/mybatis-plus-single-v1/controller.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/entity.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/mapper.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/service.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/dto.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/vo.java.ftl");
			templates.add("/templates/mybatis-plus-single-v1/service.impl.java.ftl");
		}
		GenUtils.genCode(Arrays.asList(tableNames.split(",")),templates);
	}
	public static void genCode(List<String> tableNames, List<String> templates ) throws Exception {
		init();
		List<ClassInfo> classInfos = GenUtils.getClassInfos(tableNames);
		GenUtils.genTables(classInfos, templates);
	}
	public static void genCode(List<String> tableNames) throws Exception {
		init();
		List<ClassInfo> classInfos = GenUtils.getClassInfos(tableNames);
		GenUtils.genTables(classInfos, templates);
	}

}
