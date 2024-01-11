package com.jun.plugin.framework.config;


import com.alibaba.druid.pool.DruidDataSource;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Configuration
public class JfinalActiveRecordConfigTx {
    /**
     * 主数据源名称
     */
    private static final String MAIN_DATA_SOURCE_CONFIG = "main";

    /**
     * 业务数据源名称
     */
    private static final String BIZ_DATA_SOURCE_CONFIG = "biz";

    @Bean
    @ConfigurationProperties("spring.datasource.main")
    public DruidDataSource mainDataSource() {
        return new DruidDataSource();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.biz")
    public DruidDataSource bizDataSource() {
        return new DruidDataSource();
    }

    /**
     * 主数据源
     *
     * @return
     */
    @Bean
    public ActiveRecordPlugin initMainActiveRecord() {
        ActiveRecordPlugin arp = new ActiveRecordPlugin(MAIN_DATA_SOURCE_CONFIG, masterTransactionAwareDataSourceProxy());
        //        ActiveRecordPlugin arp = new ActiveRecordPlugin(MAIN_DATA_SOURCE_CONFIG, mainDataSource());
//        arp.addMapping("emp", Emp.class);
//        arp.addMapping("emp_balance", EmpBalance.class);
        arp.start();
        return arp;
    }

    /**
     * 业务数据源
     *
     * @return
     */
    @Bean
    public ActiveRecordPlugin initBizActiveRecord() {
        ActiveRecordPlugin arp = new ActiveRecordPlugin(BIZ_DATA_SOURCE_CONFIG, bizTransactionAwareDataSourceProxy());
        //        ActiveRecordPlugin arp = new ActiveRecordPlugin(BIZ_DATA_SOURCE_CONFIG, bizDataSource());
//        // 第二个数据源如何和第一个数据有相同的表，则不需要重复映射，只需要在调用时指定数据源即可
//        arp2.addMapping("emp", Emp.class);
        arp.start();
        return arp;
    }

    /**
     * 设置数据源代理
     */
    @Bean
    public TransactionAwareDataSourceProxy masterTransactionAwareDataSourceProxy() {
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy = new TransactionAwareDataSourceProxy();
        transactionAwareDataSourceProxy.setTargetDataSource(mainDataSource());
        return transactionAwareDataSourceProxy;
    }

    @Bean
    public TransactionAwareDataSourceProxy bizTransactionAwareDataSourceProxy() {
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy = new TransactionAwareDataSourceProxy();
        transactionAwareDataSourceProxy.setTargetDataSource(bizDataSource());
        return transactionAwareDataSourceProxy;
    }

    /**
     * 设置事务管理
     */
    @Bean(name = "mainDataSourceTransactionManager")
    public DataSourceTransactionManager masterDataSourceTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(masterTransactionAwareDataSourceProxy());
        return dataSourceTransactionManager;
    }

    @Bean(name = "bizDataSourceTransactionManager")
    public DataSourceTransactionManager bizDataSourceTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(bizTransactionAwareDataSourceProxy());
        return dataSourceTransactionManager;
    }
}


//@Service
//public class EmpBalanceService {
//
//    @Transactional(transactionManager="mainDataSourceTransactionManager", rollbackFor = Exception.class)
//    public void addBalance() {
//        // 这个应该不能保存进表中才对
//        new EmpBalance().set("emp_id", 1)
//                .set("balance", 10).save();
//
//        Emp emp = Emp.dao.findById(1);
//
//        // 这里故意写错，以测试实务
//        emp.set("name", "超出长度的字符串用于测试错误").update();
//    }
//}