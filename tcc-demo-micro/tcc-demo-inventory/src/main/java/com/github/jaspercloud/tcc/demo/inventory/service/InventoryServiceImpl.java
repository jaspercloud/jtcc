package com.github.jaspercloud.tcc.demo.inventory.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.jaspercloud.tcc.demo.inventory.dao.InventoryMapper;
import com.github.jasperlcoud.tcc.demo.api.service.InventoryService;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service(interfaceClass = InventoryService.class)
@Transactional(transactionManager = "inventory")
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    protected InventoryMapper inventoryMapper;

    @TccMethod(uniqueName = "subInventory", confirmMethod = "confirmSubInventory", cancelMethod = "cancelSubInventory")
    @Override
    public void subInventory(long id, int num) {
        Integer query = inventoryMapper.selectInventoryById(id);
        if (query < num) {
            throw new RuntimeException("库存不足");
        }
        int set = query - num;
        int update = inventoryMapper.updateInventory(id, set, query);
        if (update <= 0) {
            throw new RuntimeException();
        }
    }

    public void confirmSubInventory(long id, int num) {
    }

    public void cancelSubInventory(long id, int num) {
        Integer query = inventoryMapper.selectInventoryById(id);
        int set = query + num;
        int update = inventoryMapper.updateInventory(id, set, query);
        if (update <= 0) {
            throw new RuntimeException();
        }
    }
}
