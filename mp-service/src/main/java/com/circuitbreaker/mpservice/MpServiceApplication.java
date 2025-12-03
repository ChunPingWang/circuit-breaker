package com.circuitbreaker.mpservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MpServiceApplication.class, args);
    }
}
