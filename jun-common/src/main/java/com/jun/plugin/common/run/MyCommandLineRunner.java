package com.jun.plugin.common.run;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.StaticLog;
import com.jun.plugin.db.DataSourcePool;
import com.jun.plugin.db.record.Db;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.jun.plugin.db.DataSourcePool.main;


@Slf4j
@Order(value = 100)
@Component
public class MyCommandLineRunner implements IInitRunner {
    @Override
    public void run() throws Exception {
        System.out.println("Spring Boot应用程序启动时初始化执行的代码111");
        // 在这里可以做一些初始化的工作或其他需要在启动时执行的任务
    }


}
