package com.bluemoon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlueMoonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlueMoonApplication.class, args);
    }

}
