package com.github.jaspercloud.tcc.dubbo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TccDubboDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccDubboDemoApplication.class, args);
    }
}
