//package io.github.wujun728.db;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
///**
// * DB类，直接使用
// *
// * 2018年9月7日 created by Mao
// */
//public class DB {
//
//	private DB() {
//	}
//
//	static {
//		System.out.println("welcome to DB+Record world !");
//	}
//
//	/**
//	 * 通过主键删除
//	 *
//	 * @param tableName
//	 * @param record
//	 *
//	 */
//	public static int delete(String tableName, String pk, Object pkValue) {
//		String sqlStr = "delete from %s where %s='%s'";
//		return update(String.format(sqlStr, tableName, pk, pkValue));
//	}
//
//	/**
//	 * 通过默认主键id删除
//	 *
//	 * @param tableName
//	 * @param record
//	 *
//	 */
//	public static int delete(String tableName, Object pkValue) {
//		return delete(tableName, "id", pkValue);
//	}
//
//	/**
//	 * Record添加表中数据
//	 *
//	 * @param tableName
//	 * @param record
//	 *
//	 */
//	public static int save(String tableName, Record record) {
//		String sqlStr = "insert into %s (%s) values(%s)";
//		String fields = record.getFieldToString();
//		String values = record.getValuesToString();
//		return update(String.format(sqlStr, tableName, fields, values));
//	}
//
//	/**
//	 * Record修改表中数据,默认主键id
//	 *
//	 * @param tableName
//	 * @param record
//	 */
//	public static int update(String tableName, Record record) {
//		return update(tableName, "id", record);
//	}
//
//	/**
//	 * Record修改表中数据,指明主键
//	 *
//	 * @param tableName
//	 * @param pk
//	 * @param record
//	 *
//	 */
//	public static int update(String tableName, String pk, Record record) {
//		Set<String> kSet = record.getKeys();
//		String sqlStr = "update %s set %s where %s='%s'";
//		/** 字段1 =值1,字段2 =值2 **/
//		StringBuffer fieldBuffer = new StringBuffer();
//
//		Iterator<String> keyIt = kSet.iterator();
//		while (keyIt.hasNext()) {
//			String tKey = keyIt.next();
//			/** 主键修改忽略 **/
//			if (tKey.equals(pk))
//				continue;
//			fieldBuffer.append(tKey).append("='").append(record.getStr(tKey)).append("'");
//			if (keyIt.hasNext())
//				fieldBuffer.append(",");
//		}
//
//		return update(String.format(sqlStr, tableName, fieldBuffer.toString(), pk, record.getStr(pk)));
//	}
//
//	/**
//	 * 执行修改语句
//	 *
//	 * @param sql
//	 */
//	public static int update(String sqlStr, Object... params) {
//		Connection conn = null;
//		Statement stmt = null;
//		sqlStr = formatterSql(sqlStr, params);
//
//		try {
//			// 创建连接
//			conn = getConnection();
//			stmt = conn.createStatement();
//			stmt.executeUpdate(sqlStr);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return 0;
//		}
//		return 1;
//	}
//
//	/**
//	 * 执行查找语句
//	 *
//	 * @param sql
//	 * @param 参数补充sql里面的问号
//	 */
//	public static List<Record> find(String sqlStr, Object... params) {
//		Connection conn = null;
//		Statement stmt = null;
//		List<Record> records = new ArrayList<Record>();
//		sqlStr = formatterSql(sqlStr, params);
//
//		/** 开始连接 **/
//		try {
//			conn = getConnection();
//			stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(sqlStr);
//			ResultSetMetaData rsmd = rs.getMetaData();
//			int rowCount = rsmd.getColumnCount();
//			/** 结果集 **/
//			Record re;
//			while (rs.next()) {
//				re = new Record();
//				for (int i = 1; i <= rowCount; i++)
//					/** 存入record中 **/
//					re.put(rsmd.getColumnName(i), rs.getString(rsmd.getColumnName(i)));
//				records.add(re);
//			}
//			rs.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return records;
//	}
//
//	/**
//	 * 格式化sql语句加参数
//	 *
//	 * @param sql
//	 * @param params
//	 * @return
//	 */
//	private static String formatterSql(String sqlStr, Object... params) {
//		StringBuffer sqlBuffer = new StringBuffer(sqlStr);
//		int index = -1;
//
//		for (Object para : params) {
//			// 防止参数中有 ? 而被错误的替换掉
//			index = sqlBuffer.indexOf("?", index + 2);
//			if (index == -1)
//				break;
//			sqlBuffer.replace(index, index + 1, "'" + String.valueOf(para) + "'");
//		}
//		return sqlBuffer.toString();
//	}
//
//	/**
//	 * 获取连接
//	 *
//	 * @throws SQLException
//	 **/
//	private static Connection getConnection() throws SQLException {
//		return C3p0Util.getConn();
//	}
//
//}
