package com.github.jaspercloud.tcc.demo.inventory;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@DubboComponentScan("com.github.jaspercloud.tcc.demo.inventory.service")
public class TccDemoInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccDemoInventoryApplication.class, args);
    }
}
