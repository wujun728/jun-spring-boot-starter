package io.github.wujun728.jdbc;

import io.github.wujun728.jdbc.page.IPageHandle;
import io.github.wujun728.jdbc.support.AbstractSqlSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseDao<T, ID> extends AbstractSqlSupport<T, ID> {

    /**
     * JdbcTemplate
     */
    @Autowired
    private  JdbcTemplate jdbcTemplate;

    /**
     * 分页处理
     */
    @Autowired
    private IPageHandle pageHandle;

//    public BaseDao() {
//        super.setEntityClass(clazz);
//    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected IPageHandle getPageHandle() {
        return this.pageHandle;
    }
}
