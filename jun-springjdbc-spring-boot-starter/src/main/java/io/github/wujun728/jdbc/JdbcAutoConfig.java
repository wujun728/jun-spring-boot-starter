package io.github.wujun728.jdbc;

import io.github.wujun728.jdbc.page.*;
import io.github.wujun728.jdbc.util.DbType;
import io.github.wujun728.jdbc.util.DbTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbcAutoConfig {
    final static Logger logger = LoggerFactory.getLogger(JdbcAutoConfig.class);

    @ConditionalOnMissingBean(IPageHandle.class)
    @Bean
    public IPageHandle pageHandle(@Autowired DataSource dataSource) {
        DbType dbType = DbTypeUtils.getDbType(dataSource);
        if (logger.isInfoEnabled()) {
            logger.info("JdbcAutoConfig pageHandle dbType={}", dbType.getName());
        }
        IPageHandle pageHandle;
        if (dbType == DbType.MYSQL) {
            pageHandle = new MysqlPageHandleImpl();
        } else if (dbType == DbType.DB2) {
            pageHandle = new DB2PageHandleImpl();
        } else if (dbType == DbType.ORACLE) {
            pageHandle = new OraclePageHandleImpl();
        } else if (dbType == DbType.POSTGRE_SQL) {
            pageHandle = new PostgreSqlPageHandleImpl();
        } else if (dbType == DbType.SQLITE) {
            pageHandle = new SqlitePageHandleImpl();
        } else if (dbType == DbType.H2) {
            pageHandle = new H2PageHandleImpl();
        } else {
            pageHandle = new MysqlPageHandleImpl();
        }
        if (logger.isInfoEnabled()) {
            logger.info("JdbcAutoConfiguration pageHandle is running!");
        }
        return pageHandle;
    }
}