package com.japsercloud.tcc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TccServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccServerApplication.class, args);
    }
}
