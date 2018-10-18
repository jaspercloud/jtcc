package com.github.jaspercloud.tcc.demo.account;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@DubboComponentScan("com.github.jaspercloud.tcc.demo.account.service")
public class TccDemoAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccDemoAccountApplication.class, args);
    }
}
