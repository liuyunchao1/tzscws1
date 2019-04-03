package com.wondersgroup.tzscws1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wondersgroup.tzscws1.dao")
@SpringBootApplication
public class Tzscws1Application {
    public static void main(String[] args) {
        SpringApplication.run(Tzscws1Application.class, args);
    }

}
