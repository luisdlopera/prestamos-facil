package com.prestamosfacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrestamosFacilApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrestamosFacilApplication.class, args);
    }
}
