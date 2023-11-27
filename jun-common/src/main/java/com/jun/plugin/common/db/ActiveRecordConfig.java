package com.jun.plugin.common.db;

import javax.annotation.Resource;
import javax.sql.DataSource;

import com.jfinal.template.source.ClassPathSourceFactory;
import org.springframework.context.annotation.Bean;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class ActiveRecordConfig {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ActiveRecordPlugin init() {
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
        //arp.addSqlTemplate("sql/all.sql");
        arp.setShowSql(false);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.start();
        return arp;
    }

}
