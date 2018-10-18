package com.github.order.tcc.demo.order;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@DubboComponentScan("com.github.order.tcc.demo.order.service")
public class TccDemoOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccDemoOrderApplication.class, args);
    }
}
