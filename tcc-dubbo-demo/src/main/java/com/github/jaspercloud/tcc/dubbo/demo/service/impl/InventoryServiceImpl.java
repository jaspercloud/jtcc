package com.github.jaspercloud.tcc.dubbo.demo.service.impl;

import com.github.jaspercloud.tcc.dubbo.demo.dao.inventory.InventoryMapper;
import com.github.jaspercloud.tcc.dubbo.demo.exception.InvokeException;
import com.github.jaspercloud.tcc.dubbo.demo.service.InventoryService;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.support.idempotent.TccMethodLogDao;
import com.jaspercloud.tcc.client.support.persistence.TccMethodDataDao;
import com.jaspercloud.tcc.core.exception.TccException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "inventory")
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    protected InventoryMapper inventoryMapper;

    @Autowired
    protected TccMethodLogDao tccMethodLogDao;

    @Autowired
    protected TccMethodDataDao tccMethodDataDao;

    @TccMethod(uniqueName = "subInventory", confirmMethod = "confirmSubInventory", cancelMethod = "cancelSubInventory")
    @Override
    public void subInventory(long id, int num) {
        Integer query = inventoryMapper.selectInventoryById(id);
        if (query < num) {
            throw new InvokeException("库存不足");
        }
        int set = query - num;
        int update = inventoryMapper.updateInventory(id, set, query);
        if (update <= 0) {
            throw new TccException();
        }
    }

    public void confirmSubInventory(long id, int num) {
    }

    public void cancelSubInventory(long id, int num) {
        Integer query = inventoryMapper.selectInventoryById(id);
        int set = query + num;
        int update = inventoryMapper.updateInventory(id, set, query);
        if (update <= 0) {
            throw new TccException();
        }
    }
}
