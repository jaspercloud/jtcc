package com.github.jaspercloud.tcc.dubbo.demo.entity;

import java.io.Serializable;

public class Order implements Serializable {

    private Long userId;
    private Long commodityId;
    private Integer price;
    private Integer num;

    public Long getUserId() {
        return userId;
    }

    public Long getCommodityId() {
        return commodityId;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getNum() {
        return num;
    }

    public Order() {
    }

    public Order(Long userId, Long commodityId, Integer price, Integer num) {
        this.userId = userId;
        this.commodityId = commodityId;
        this.price = price;
        this.num = num;
    }
}
