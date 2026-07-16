package com.badminton;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 羽毛球馆综合服务系统后端启动类
 */
@SpringBootApplication
@MapperScan("com.badminton.mapper")
@EnableScheduling
public class BadmintonServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonServerApplication.class, args);
    }
}
