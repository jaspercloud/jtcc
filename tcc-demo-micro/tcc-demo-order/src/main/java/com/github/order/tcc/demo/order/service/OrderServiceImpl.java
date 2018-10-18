package com.github.order.tcc.demo.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.jasperlcoud.tcc.demo.api.entity.Order;
import com.github.jasperlcoud.tcc.demo.api.service.AccountService;
import com.github.jasperlcoud.tcc.demo.api.service.InventoryService;
import com.github.jasperlcoud.tcc.demo.api.service.OrderService;
import com.github.order.tcc.demo.order.dao.OrderMapper;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.support.persistence.TccMethodDataDao;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Reference
    protected AccountService accountService;

    @Reference
    protected InventoryService inventoryService;

    @Autowired
    protected OrderMapper orderMapper;

    @Autowired
    protected TccMethodDataDao tccMethodDataDao;

    public OrderServiceImpl() {
        System.out.println();
    }

    @TccMethod(uniqueName = "createOrder", confirmMethod = "confirmOrder", cancelMethod = "cancelOrder")
    @Override
    public void createOrder(Order order) {
        Integer price = order.getPrice();
        Integer num = order.getNum();
        Integer total = price * num;

        accountService.subCoin(order.getUserId(), total);
        inventoryService.subInventory(order.getCommodityId(), num);

        Long oderId = orderMapper.selectId();
        orderMapper.insertOrder(oderId, price, num, total, "init");

        Map<String, String> map = new HashMap<>();
        map.put("orderId", String.valueOf(oderId));
        tccMethodDataDao.saveData(map);
    }

    public void confirmOrder(Order order) {
        Map<String, String> data = tccMethodDataDao.getData();
        long orderId = NumberUtils.toLong(data.get("orderId"));
        orderMapper.updateOrderStatus(orderId, "success");
//        tccMethodDataDao.deleteData();
    }

    public void cancelOrder(Order order) {
        Map<String, String> data = tccMethodDataDao.getData();
        long orderId = NumberUtils.toLong(data.get("orderId"));
        orderMapper.updateOrderStatus(orderId, "cancel");
//        tccMethodDataDao.deleteData();
    }
}
