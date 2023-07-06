package com.gitthub.wujun728.engine.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ArrayUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.sql.*;
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

	public static Properties props = new Properties(); // 配置文件
	public static String PROJECT_PATH;// 项目在硬盘上的基础路径，项目路径
	public static String JAVA_PATH; // java文件路径
	public static String RESOURCES_PATH;// 资源文件路径
	public static String PAGE_PATH;// 资源文件路径
	public static String PACKAGE;// 资源文件路径
	public static String TEMPLATE_FILE_PATH = PROJECT_PATH + "/src/main/resources/templates";// 模板位置

	static {
		try {
			PROJECT_PATH = FileUtil.getParent(GenUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(),2);
			InputStream is = GenUtils.class.getClassLoader().getResourceAsStream("config.properties");
			props.load(is);
			Class.forName(props.getProperty("driver"));
			PACKAGE = props.getProperty("basePackage");
			JAVA_PATH = props.getProperty("javaPath");
			RESOURCES_PATH = props.getProperty("resourcesPath");
			PAGE_PATH = props.getProperty("pagePath");
			PROJECT_PATH = props.getProperty("PROJECT_PATH");
			TEMPLATE_FILE_PATH = PROJECT_PATH + props.getProperty("templateFilePath");// 模板位置
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static List<String> getFilePaths(List<String> templates, ClassInfo classInfo) {
        List<String> filePaths = new ArrayList<>();
		for(String template : templates){
			String path_tmep = FileNameUtil.getName(template).replace(".ftl","");
			String filename_tmep = upperCaseFirstWordV2(path_tmep);
			String packageName = PACKAGE+"."+path_tmep;
			String package2Path = String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
			filePaths.add(PROJECT_PATH + JAVA_PATH + package2Path + classInfo.getClassName() + filename_tmep + ".java");
		}
        return filePaths;
    }

	public static void getFile(String path, List<Map<String, Object>> list) {
		File file = new File(path);
		File[] array = file.listFiles();
		for (int i = 0; i < array.length; i++) {
			if (array[i].isFile()) {
				Map<String, Object> map = new HashMap<String, Object>();
				// only take file name
				// System.out.println("^^^^^" + array[i].getName());
				// take file path and name
				// System.out.println("*****" + array[i].getPath());
				map.put(array[i].getName(), array[i].getPath());
				list.add(map);
			} else if (array[i].isDirectory()) {
				getFile(array[i].getPath(), list);
			}
		}
	}

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

	public static String firstLower(String str) {
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	public static String replaceTabblePreStr(String str) {
//		str = str.toLowerCase().replaceFirst("tab_", "").replaceFirst("tb_", "").replaceFirst("t_", "");
		for (String x : props.getProperty("tableRemovePrefixes").split(",")) {
			if(str.startsWith(x.toLowerCase())){
				str = str.replaceFirst(x.toLowerCase(), "");
			}
		}
		return str;
	}

	public static String replaceRowPreStr(String str) {
//		str = str.toLowerCase().replaceFirst("tab_", "").replaceFirst("tb_", "").replaceFirst("t_", "");
		for (String x : props.getProperty("rowRemovePrefixes").split(",")) {
			str = str.replaceFirst(x.toLowerCase(), "");
		}
		return str;
	}

	public static String simpleName(String type) {
		return type.replace("java.lang.", "").replaceFirst("java.util.", "");
	}

	public static String upperCaseFirstWord(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
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
	
	
	//****************************************************************************************************


	public static void processTemplatesFileWriter(ClassInfo classInfo, Map<String, Object> datas, List<String> templates) throws IOException, TemplateException {
		for(int i = 0 ; i < templates.size() ; i++) {
			GenUtils.processFile(templates.get(i), datas, GenUtils.getFilePaths(templates,classInfo).get(i));
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

	private static freemarker.template.Configuration getConfiguration() throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(
				freemarker.template.Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(new File(GenUtils.TEMPLATE_FILE_PATH));
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

	//****************************************************************************************************

	/***
	 * 构建 Java文件，遍历文件夹下所有的模板，然后生成对应的文件（需要配置模板的package及path）
	 */
	public static void batchBuilderByDirectory1111(Map<String, Object> modelMap) {
		List<Map<String, Object>> srcFiles = new ArrayList<Map<String, Object>>();
		String TEMPLATE_PATH = GenUtils.class.getClassLoader().getResource("").getPath()
		.replace("/target/classes/", "") + "/src/main/resources/" + props.getProperty("template_path");
		getFile(TEMPLATE_PATH, srcFiles);
		for (int i = 0; i < srcFiles.size(); i++) {
			HashMap<String, Object> m = (HashMap<String, Object>) srcFiles.get(i);
			Set<String> set = m.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if ((boolean) modelMap.get("Swagger") == true) {
					if (!key.contains(".json")) {
						continue;
					}
				}
				String templateFileName = key;
				String templateFileNameSuffix = key.substring(key.lastIndexOf("."), key.length());
				String templateFileNamePrefix = key.substring(0, key.lastIndexOf("."));
				String templateFilePathAndName = String.valueOf(m.get(key));
				String templateFilePath = templateFilePathAndName.replace("\\" + templateFileName, "");
				String templateFilePathMiddle = "";
				if (!templateFilePath.endsWith(props.getProperty("template_path").replace("/", "\\"))) {
					templateFilePathMiddle = templateFilePath
							.substring(templateFilePath.lastIndexOf("\\"), templateFilePath.length()).replace("\\", "");
				}
				if (key.contains(".json")) {
					//logger.info("templateFilePath=" + templateFilePath);
					continue;
				}
				try {
					String path = null;
					if (templateFileNameSuffix.equalsIgnoreCase(".java")) {
						// 创建文件夹
						path = GenUtils.PROJECT_PATH + "/" + PACKAGE.replace(".", "/") + "/" + templateFileNamePrefix.toLowerCase();
					}
					if (templateFileNameSuffix.equalsIgnoreCase(".ftl")) {
						path = GenUtils.PROJECT_PATH + "/" + PACKAGE.replace(".", "/") + "/" + templateFilePathMiddle + "/";
					}
					String fileNameNew = templateFileNamePrefix
							.replace("${className}", String.valueOf(modelMap.get("Table")))
							.replace("${classNameLower}", String.valueOf(modelMap.get("Table")).toLowerCase());
					// 创建文件
//					GeneratorUtils.writer(template, modelMap, path + "/" + fileNameNew);
					GenUtils.processFile(templateFileName, modelMap, path + "/" + fileNameNew);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}




	public static List<ClassInfo> getClassInfo(String[] tables) {
		return getClassInfo(tables, null);
	}

	@SuppressWarnings("unchecked")
	public static List<ClassInfo> getClassInfo(String[] tables, Connection conn) {
		List<ClassInfo> list = new ArrayList<ClassInfo>();
		try {
			if (conn == null) {
				conn = DriverManager.getConnection(GenUtils.props.getProperty("url"),
						GenUtils.props.getProperty("username"), GenUtils.props.getProperty("password"));
			}
			DatabaseMetaData metaData = conn.getMetaData();
			String databaseType = metaData.getDatabaseProductName(); // 获取数据库类型：MySQL
			// 针对MySQL数据库进行相关生成操作
			if (databaseType.equals("MySQL")) {
				// 获取所有表结构
				ResultSet tableResultSet = metaData.getTables(conn.getCatalog(), conn.getSchema() /* "%" */, "%",
						new String[] { "TABLE" });
				// 获取数据库名字
				String database = conn.getCatalog();
				while (tableResultSet.next()) {
					// 循环所有表信息
					String tableName = tableResultSet.getString("TABLE_NAME"); // 获取表名
					if (tables == null || ArrayUtil.containsIgnoreCase(tables, tableName)) {
						/**判断字段是否自增*/
						String sql = "select * from " + tableName + " where 1=2";
						ResultSet rst = conn.prepareStatement(sql).executeQuery();
						ResultSetMetaData rsmd = rst.getMetaData();
						int i=1;
//						List<String> pkList = getPrimaryKeysInfo(metaData, tableName);
						String table = GenUtils.replace_(GenUtils.replaceTabblePreStr(tableName)); // 名字操作,去掉tab_,tb_，去掉_并转驼峰
						String Table = GenUtils.firstUpper(table); // 获取表名,首字母大写
						String tableComment = tableResultSet.getString("REMARKS"); // 获取表备注
						String className = GenUtils.replace_(GenUtils.replaceTabblePreStr(tableName)); // 名字操作,去掉tab_,tb_，去掉_并转驼峰
						String classNameFirstUpper = GenUtils.firstUpper(className); // 大写对象
//						showTableInfo(tableResultSet);
						log.info("当前表名：" + tableName);
						Set<String> typeSet = new HashSet<String>(); // 所有需要导包的类型
						ResultSet cloumnsSet = metaData.getColumns(database, GenUtils.props.getProperty("username"),
								tableName, null); // 获取表所有的列
//						ResultSet keySet = metaData.getPrimaryKeys(database, GenUtils.props.getProperty("username"),
//								tableName); // 获取主键
//						String key = "", keyType = "";
//						while (keySet.next()) {
//							key = keySet.getString(4);
//						}
						// V1 初始化数据及对象 模板V1 field List
						List<FieldInfo> fieldList = new ArrayList<FieldInfo>();
						List<FieldInfo> pkfieldList = new ArrayList<FieldInfo>();
						while (cloumnsSet.next()) {
							String remarks = cloumnsSet.getString("REMARKS");// 列的描述
							String columnName = cloumnsSet.getString("COLUMN_NAME"); // 获取列名
							String javaType = GenUtils.getType(cloumnsSet.getInt("DATA_TYPE"));// 获取类型，并转成JavaType
							String typeName = cloumnsSet.getString("TYPE_NAME"); //
							int COLUMN_SIZE = cloumnsSet.getInt("COLUMN_SIZE");// 获取
							//String TABLE_SCHEM = cloumnsSet.getString("TABLE_SCHEM");// 获取
							//String COLUMN_DEF = cloumnsSet.getString("COLUMN_DEF");// 获取
							int NULLABLE = cloumnsSet.getInt("NULLABLE");// 获取
							// int DATA_TYPE = cloumnsSet.getInt("DATA_TYPE");// 获取
							String defaultValue = cloumnsSet.getString("COLUMN_DEF");//字段默认值
							// showColumnInfo(cloumnsSet);
							String propertyName = GenUtils.replace_(GenUtils.replaceRowPreStr(columnName));// 处理列名，驼峰
							typeSet.add(javaType);// 需要导包的类型
							Boolean isPk = false;
//							if (!CollectionUtils.isEmpty(pkList) && pkList.contains(columnName)) {
//								isPk = true;
//							}
							// V1 初始化数据及对象
							FieldInfo fieldInfo = new FieldInfo();
							fieldInfo.setColumnName(columnName);
							fieldInfo.setFieldName(propertyName);
							fieldInfo.setFieldClass(GenUtils.simpleName(javaType));
							fieldInfo.setFieldComment(remarks);
							fieldInfo.setColumnSize(COLUMN_SIZE);
							fieldInfo.setNullable(NULLABLE == 0);
							fieldInfo.setFieldType(javaType);
							fieldInfo.setColumnType(typeName);
//							fieldInfo.setIsPrimaryKey(isPk);
							fieldInfo.setDefaultValue(defaultValue);
							boolean isAutoIncrement = rsmd.isAutoIncrement(i); //自增
							fieldInfo.setIsAutoIncrement(isAutoIncrement);
							fieldList.add(fieldInfo);
							if(isPk) {
								pkfieldList.add(fieldInfo);
							}
							i++;
						}
						// ************************************************************************
						if (fieldList != null && fieldList.size() > 0) {
							ClassInfo classInfo = new ClassInfo();
							classInfo.setTableName(tableName);
							classInfo.setClassName(classNameFirstUpper);
							classInfo.setClassComment(tableComment);
							classInfo.setFieldList(fieldList);
							classInfo.setPkSize(pkfieldList.size());
							//classInfo.setPkSize(pkList.size());
							list.add(classInfo);
						}
						// ************************************************************************
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}


}
