package com.xammer.billops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// Add this annotation to explicitly scan all your project's packages
@ComponentScan(basePackages = "com.xammer.billops.*")
public class BillopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillopsApplication.class, args);
    }

}