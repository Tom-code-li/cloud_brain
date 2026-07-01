package com.doctor.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.doctor.platform.**.mapper")
public class DoctorPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoctorPlatformApplication.class, args);
    }
}
