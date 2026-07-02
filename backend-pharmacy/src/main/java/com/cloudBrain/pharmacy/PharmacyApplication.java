package com.cloudBrain.pharmacy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@MapperScan("com.cloudBrain.pharmacy.mapper")
public class PharmacyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyApplication.class, args);
    }

}