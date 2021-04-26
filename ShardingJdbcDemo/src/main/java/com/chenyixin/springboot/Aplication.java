package com.chenyixin.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;



@SpringBootApplication(
        scanBasePackages = {"com.chenyixin.springboot"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Aplication {

    public static void main(String[] args) {
        SpringApplication.run(Aplication.class, args);
    }

}