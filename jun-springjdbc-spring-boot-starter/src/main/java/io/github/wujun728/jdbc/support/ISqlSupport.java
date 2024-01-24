package io.github.wujun728.jdbc.support;

import io.github.wujun728.jdbc.page.Page;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * sql操作接口，传入要执行的SQL和要绑定到SQL的参数，操纵数据库，执行增、删、改、查操作
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-07-19
 **/
public interface ISqlSupport<T,ID> {

    /**
     * 使用提供的SQL语句和提供的参数，执行增删改
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到SQL的参数
     * @return int 受影响的行数
     */
    int execute(String sql, Object... params);

    /**
     * 使用提供的SQL语句和提供的参数，执行删
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到SQL的参数
     * @return int 受影响的行数
     */
    int delete(String sql, final Object... params);

    /**
     * 使用提供的SQL语句和提供的参数，执行改
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到SQL的参数
     * @return int 受影响的行数
     */
    int update(String sql, final Object... params);

    /**
     * 使用提供的SQL语句和提供的参数，执行增
     *
     * @param sql    要执行的SQL
     * @param params 要绑定到SQL的参数
     * @return int 受影响的行数
     */
    int insert(String sql, final Object... params);


    /**
     * 查询给定的SQL和参数列表，返回实例列表
     *
     * @param sql    要执行的SQL查询
     * @param params 要绑定到查询的参数
     * @return List<T> 实例列表
     */
    List<T> select(String sql, Object... params);
    List<T> select(String sql);

    /**
     * 查询给定的SQL和参数列表，返回实例列表
     *
     * @param sql    要执行的SQL查询
     * @param params 要绑定到查询的参数
     * @param clazz  实体类
     * @return List<F> 实例列表
     */
    <F> List<F> select(String sql, Class<F> clazz, Object... params);

    /**
     * 分页查询（带参数）
     *
     * @param sql    要执行的SQL
     * @param clazz  实体类型
     * @param pageNumber 当前页
     * @param pageSize   页大小
     * @param params     ？参数
     * @return Page<F>
     */
    <F> Page<F> paginate(String sql, Class<F> clazz, Integer pageNumber, Integer pageSize, final Object... params);

    /**
     * 查询给定的SQL和参数列表，结果返回第一条
     *
     * @param sql    要执行的SQL查询
     * @param params 要绑定到查询的参数
     * @return T 实例
     */
    default T selectOne(String sql, Object... params) {
        List<T> resultList = this.select(sql, params);
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.get(0);
        }
        return null;
    }

    /**
     * 查询给定的SQL和参数列表，结果返回第一条
     *
     * @param sql    要执行的SQL查询
     * @param params 要绑定到查询的参数
     * @param clazz  实体类
     * @return F 实例
     */
    default <F> F selectOne(String sql, Class<F> clazz, Object... params) {
        List<F> resultList = this.select(sql, clazz, params);
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.get(0);
        }
        return null;
    }

    /**
     * 执行查询sql，有查询条件，（固定返回List<Map<String, Object>>）
     *
     * @param sql    要执行的sql
     * @param params 要绑定到查询的参数
     * @return Map<String, Object>
     */
    List<Map<String, Object>> selectMap(String sql, Object... params);

    /**
     * 执行查询sql，有查询条件，结果返回第一条（固定返回Map<String, Object>）
     *
     * @param sql    要执行的sql
     * @param params 要绑定到查询的参数
     * @return Map<String, Object>
     */
    Map<String, Object> selectOneMap(String sql, Object... params);

    /**
     * 查询一个值（经常用于查count）
     *
     * @param sql    要执行的SQL查询
     * @param clazz  实体类
     * @param params 要绑定到查询的参数
     * @param <F>    泛型
     * @return F
     */
    <F> F selectOneColumn(String sql, Class<F> clazz, Object... params);

    /**
     * 分页查询
     *
     * @param sql 要执行的SQL查询
     * @return T
     */
    Page<T> paginate(String sql, Integer pageNumber, Integer pageSize);

    /**
     * 分页查询
     *
     * @param sql 要执行的SQL查询
     * @return T
     */
    Page<T> paginate(String sql, Integer pageNumber, Integer pageSize, Object... params);

    /**
     * 使用 in 进行批量操作，比如批量启用，批量禁用，批量删除等 -- 更灵活的就需要自己写了
     *
     * @param sql    示例： update s_url_map set del_flag = '1' where id in (:idList)
     * @param idList 一般为 List<String> 或 List<Integer>
     * @return 执行的结果条数
     */
    int batchOpera(String sql, List<Object> idList);
}