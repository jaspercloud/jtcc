package com.github.jaspercloud.tcc.dubbo.demo.service.impl;

import com.github.jaspercloud.tcc.dubbo.demo.dao.order.OrderMapper;
import com.github.jaspercloud.tcc.dubbo.demo.entity.Order;
import com.github.jaspercloud.tcc.dubbo.demo.service.AccountService;
import com.github.jaspercloud.tcc.dubbo.demo.service.InventoryService;
import com.github.jaspercloud.tcc.dubbo.demo.service.OrderService;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.support.idempotent.TccMethodLogDao;
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

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected InventoryService inventoryService;

    @Autowired
    protected OrderMapper orderMapper;

    @Autowired
    protected TccMethodLogDao tccMethodLogDao;

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
