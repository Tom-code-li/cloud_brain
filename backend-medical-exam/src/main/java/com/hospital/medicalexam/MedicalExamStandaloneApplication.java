package com.hospital.medicalexam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.hospital.medicalexam.mapper")
public class    MedicalExamStandaloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalExamStandaloneApplication.class, args);
    }
}
