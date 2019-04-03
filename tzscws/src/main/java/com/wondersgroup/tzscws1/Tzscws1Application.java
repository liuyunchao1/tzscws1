package com.wondersgroup.tzscws1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.wondersgroup.tzscws1.dao")
@SpringBootApplication
@EnableScheduling
public class Tzscws1Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Tzscws1Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Tzscws1Application.class);
    }
}
