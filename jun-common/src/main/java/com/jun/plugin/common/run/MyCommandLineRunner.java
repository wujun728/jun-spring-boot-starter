package com.jun.plugin.common.run;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Order(value = 100)
@Component
public class MyCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Spring Boot应用程序启动时执行的代码111");
        // 在这里可以做一些初始化的工作或其他需要在启动时执行的任务
    }
}