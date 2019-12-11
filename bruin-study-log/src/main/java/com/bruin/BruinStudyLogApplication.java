package com.bruin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.bruin.mapper")
public class BruinStudyLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BruinStudyLogApplication.class, args);
    }

}
