package com.chenyixin.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;


@SpringBootApplication(
        scanBasePackages = {"com.chenyixin.springboot"},
        exclude = {DruidDataSourceAutoConfigure.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Aplication {

    public static void main(String[] args) {
        SpringApplication.run(Aplication.class, args);
    }

}