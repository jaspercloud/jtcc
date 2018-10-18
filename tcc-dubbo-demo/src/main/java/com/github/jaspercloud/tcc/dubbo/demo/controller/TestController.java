package com.github.jaspercloud.tcc.dubbo.demo.controller;

import com.github.jaspercloud.tcc.dubbo.demo.entity.Order;
import com.github.jaspercloud.tcc.dubbo.demo.service.OrderService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class TestController implements InitializingBean {

    @Autowired
    private OrderService orderService;

    private AtomicBoolean autoInvoke = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 300; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (!autoInvoke.get()) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                        try {
                            tcc();
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @RequestMapping("/tcc")
    public void tcc() {
        Order order = new Order(1L, 1L, 1, 1);
        orderService.createOrder(order);
    }

    @RequestMapping("/enableAutoInvoke")
    public void enableAutoInvoke() {
        autoInvoke.set(true);
    }

    @RequestMapping("/disableAutoInvoke")
    public void disableAutoInvoke() {
        autoInvoke.set(false);
    }
}
