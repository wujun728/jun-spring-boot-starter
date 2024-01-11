# Db+Record模式

## 项目介绍
仿jfinal的Db+Record模式。
>Jfinal：Db 类及其配套的 Record 类，提供了在 Model 类之外更为丰富的数据库操作功能。使用
Db 与 Record 类时，无需对数据库表进行映射，Record 相当于一个通用的 Model。
## 更新日志

#### 2018/9/11 14:51
1. 添加了c3p0数据库连接池
2. 删除了原来DB的配置类，和对应的配置文件 `DB_Record.config`
3. 添加了新的配置文件 `c3p0.properties`

#### 2018/9/10 8:57
1. 添加了DB的配置类，以及配置文件
2. 删除了InitDB方法,直接可以使用DB
3. 配置文件在 `DB_Record.config`

#### 2018/9/9 9:52
1. 添加了Record中的tablename,pk字段
2. 添加了Record的update、save、delete方法，增强了灵活性

#### 2018/9/8 14:35
1. 添加了DB的Record添加，修改功能
2. 添加了DB中按主键删除功能

#### 2018/9/8 11:19
1. 添加了更新功能
2. 添加支持不定参数

#### 2018/9/7 23:11
1. 添加DB+Record项目
2. 实现了Record基本存储
3. 实现了DB的查功能

## 实现功能
* 实现了DB静态类操作数据库
* 实现了Record对单条数据的存储

## 开发语言
* java

## 运行环境
* jdk8
* mysql

## 使用流程
1. 下载项目
2. 将里面的DB.java、Record.java导入java项目中
3. 导入mysql-connector-java-x.x.xx-bin.jar
4. 再合适的地方初始化DB
5. 使用DB,Record操作数据库

## 配置文件说明
配置名|意义
---|---
driverName|驱动名称
jdbcUrl|数据库连接串
user|数据库用户名
password|数据库密码


## DB常用函数原型
```java 
        /*********************************增************************************/
	/**
	 * Record添加表中数据
	 * @param tableName
	 * @param record
	 */
	public static int save(String tableName, Record record);

        /*********************************删************************************/
	/**
	 * 通过主键删除
	 * @param tableName 表名
	 * @param pk 主键
	 * @param record 记录
	 */
	public static int delete(String tableName, String pk, Object pkValue);

	/**
	 * 通过默认主键id删除
	 * @param tableName 表名
	 * @param record 记录
	 */
	public static int delete(String tableName, Object pkValue);

        /*********************************改************************************/
	/**
	 * Record修改表中一条数据,指明主键
	 * @param tableName 表名
	 * @param pk 主键
	 * @param record 记录
	 */
	public static int update(String tableName, String pk, Record record);

	/**
	 * Record修改表中一条数据,默认主键id
	 * @param tableName
	 * @param record
	 */
	public static int update(String tableName, Record record);

	/**
	 * 执行更新数据库语句
	 * @param sql
	 * @param params 参数列表
	 */
	public static int update(String sqlStr, Object... params);

        /*********************************查************************************/
	 /**
	 * 执行查找语句
	 * @param sql 欲查找的sql语句
	 * @param params 参数列表
	 */
	public static List<Record> find(String sqlStr, Object... params);

```

## Record常用常用函数原型
```java

	/** 添加Record，需要tablename字段不为空**/

	public int save();


	/** 删除Record，需要tablename字段不为空,默认主键为id **/
	public int delete();


	/** 修改Record,需要tablename字段不为空**/ **/
	public int update();

	/**
	 * 获取字段的值
	 * @param key 字段名
	 */
	public String getStr(String key);


	/**
	 * 设置字段值
	 * @param key 字段名
	 * @param value 值
	 */
	public Record put(String key, Object value);

	/**
	 * 获取所有key的Set
	 * @return
	 */
	public Set<String> getKeys();

	/**
	 * 获取所有value数组
	 */
	public Object[] getValues();

	/** 获取所属表名 **/
	public String getTableName();

	/** 设置所属表名 **/
	public Record setTableName(String tableName);

	/** 获取主键名 **/
	public String getPk();

	/** 设置主键名 **/
	public Record setPk(String pk);

```

## 实际使用

#### 使用方式

![使用方式](https://images.gitee.com/uploads/images/2018/0911/151714_bb64d7e3_1255563.png "使用方式.png")


#### 运行结果

![运行结果](https://images.gitee.com/uploads/images/2018/0911/151734_c7c08419_1255563.png "运行结果.png")

## 参与贡献
1. Mao