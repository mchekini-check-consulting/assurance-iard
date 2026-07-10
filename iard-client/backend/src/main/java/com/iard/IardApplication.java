package com.iard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IardApplication {

    public static void main(String[] args) {
        SpringApplication.run(IardApplication.class, args);
    }

}
