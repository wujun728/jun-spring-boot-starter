//package io.github.wujun728.db;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Record 类
// *
// * 2018年9月7日 created by Mao
// */
//public class Record {
//
//	/** 数据 **/
//	private Map<String, Object> datas = new HashMap<String, Object>();
//	/** 所属表名 **/
//	private String tableName = "";
//	/** 主键名 **/
//	private String pk = "";
//
//	public Record() {
//	}
//
//	/**
//	 * 构造函数设置所属表，主键
//	 *
//	 * @param tableName
//	 * @param pk
//	 */
//	public Record(String tableName, String pk) {
//		this.tableName = tableName;
//		this.pk = pk;
//	}
//
//	/** 获取所属表名 **/
//	public String getTableName() {
//		return tableName;
//	}
//
//	/** 设置所属表名 **/
//	public Record setTableName(String tableName) {
//		this.tableName = tableName;
//		return this;
//	}
//
//	/** 获取主键名 **/
//	public String getPk() {
//		return pk;
//	}
//
//	/** 设置主键名 **/
//	public Record setPk(String pk) {
//		this.pk = pk;
//		return this;
//	}
//
//	/**
//	 * 删除Record
//	 */
//	public int delete() {
//		if (this.tableName.equals("") || datas.keySet().size() == 0)
//			// 数据不全
//			return -1;
//		return DB.delete(tableName, pk.equals("") ? getStr("id") : getStr(pk));
//	}
//
//	/**
//	 * 添加Record
//	 */
//	public int save() {
//		if (this.tableName.equals("") || datas.keySet().size() == 0)
//			// 数据不全
//			return -1;
//		return DB.save(tableName, this);
//	}
//
//	/**
//	 * 保存修改的Record
//	 */
//	public int update() {
//		if (this.tableName.equals(""))
//			// 数据不全
//			return -1;
//		// 默认为id
//		else if (this.pk.equals(""))
//			return DB.update(tableName, this);
//		return DB.update(tableName, pk, this);
//	}
//
//	/**
//	 * 打印记录
//	 */
//	public String toString() {
//		Set<String> kSet = datas.keySet();
//		StringBuffer sb = new StringBuffer("{");
//		Iterator<String> It = kSet.iterator();
//		String tmpKey;
//		while (It.hasNext()) {
//			// json格式打印
//			tmpKey = It.next();
//			sb.append(tmpKey);
//			sb.append(":");
//			sb.append(datas.get(tmpKey) + ",");
//			if (!It.hasNext())
//				sb.setLength(sb.length() - 1);
//		}
//		sb.append("}");
//		return sb.toString();
//	}
//
//	/**
//	 * 设置键值
//	 *
//	 * @param key
//	 * @param value
//	 * @return this
//	 */
//	public Record put(String key, Object value) {
//		datas.put(key, value);
//		return this;
//	}
//
//	/**
//	 * 获取值
//	 *
//	 * @param key
//	 * @return value
//	 */
//	public String getStr(String key) {
//		return String.valueOf(datas.get(key));
//	}
//
//	/**
//	 * 获取所有key
//	 *
//	 * @return
//	 */
//	public Set<String> getKeys() {
//		return datas.keySet();
//	}
//
//	/**
//	 * 获取所有value
//	 *
//	 * @return
//	 */
//	public Object[] getValues() {
//		return datas.values().toArray();
//	}
//
//	/**
//	 * 获取所有value的字符串
//	 *
//	 * @return
//	 */
//	public String getValuesToString() {
//		StringBuffer valueBuffer = new StringBuffer();
//		Iterator<Object> valueIt = datas.values().iterator();
//		while (valueIt.hasNext()) {
//			String value = String.valueOf(valueIt.next());
//			valueBuffer.append("'").append(value).append("'");
//			if (valueIt.hasNext())
//				valueBuffer.append(",");
//		}
//		return valueBuffer.toString();
//	}
//
//	/**
//	 * 获取所有字段的字符串
//	 *
//	 * @return
//	 */
//	public String getFieldToString() {
//		StringBuffer fieldBuffer = new StringBuffer();
//		Iterator<String> fieldIt = datas.keySet().iterator();
//		while (fieldIt.hasNext()) {
//			String field = String.valueOf(fieldIt.next());
//			fieldBuffer.append(field);
//			if (fieldIt.hasNext())
//				fieldBuffer.append(",");
//		}
//
//		return fieldBuffer.toString();
//	}
//
//}
