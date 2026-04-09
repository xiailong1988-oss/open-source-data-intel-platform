package com.haidian.intel.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端服务启动入口。
 * 当前阶段只搭建可运行骨架，不提前扩展具体业务模块实现。
 */
@SpringBootApplication
public class HaidianIntelPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaidianIntelPlatformApplication.class, args);
    }
}
