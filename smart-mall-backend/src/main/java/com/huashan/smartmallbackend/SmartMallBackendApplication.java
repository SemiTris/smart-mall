package com.huashan.smartmallbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 「智选商城」智能客服系统 - 启动类
 *
 * @author hs
 */
@SpringBootApplication
@MapperScan("com.huashan.smartmallbackend.mapper")
public class SmartMallBackendApplication {

    /**
     * 程序入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartMallBackendApplication.class, args);
        System.out.println("""
                ============================================================
                  智选商城 · 智能客服系统 启动成功
                  前端地址：http://localhost:5173
                  接口地址：http://localhost:8080/...   （无 /api 前缀）
                  数据库  ：smart_mall（SQL 见 resources/mapper/*.xml）
                ============================================================
                """);
    }
}