package com.github.jaspercloud.tcc.dubbo.demo.service.impl;

import com.github.jaspercloud.tcc.dubbo.demo.dao.account.AccountMapper;
import com.github.jaspercloud.tcc.dubbo.demo.exception.InvokeException;
import com.github.jaspercloud.tcc.dubbo.demo.service.AccountService;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.support.idempotent.TccMethodLogDao;
import com.jaspercloud.tcc.client.support.persistence.TccMethodDataDao;
import com.jaspercloud.tcc.core.exception.TccException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "account")
public class AccountServiceImpl implements AccountService {

    @Autowired
    protected AccountMapper accountMapper;

    @Autowired
    protected TccMethodLogDao tccMethodLogDao;

    @Autowired
    protected TccMethodDataDao tccMethodDataDao;

    @TccMethod(uniqueName = "subCoin", confirmMethod = "confirmSubCoin", cancelMethod = "cancelSubCoin")
    @Override
    public void subCoin(long id, int coin) {
        Integer query = accountMapper.selectCoinById(id);
        if (query < coin) {
            throw new InvokeException("余额不足");
        }
        int set = query - coin;
        int update = accountMapper.updateCoin(id, set, query);
        if (update <= 0) {
            throw new TccException();
        }
    }

    public void confirmSubCoin(long id, int coin) {
    }

    public void cancelSubCoin(long id, int coin) {
        Integer query = accountMapper.selectCoinById(id);
        int set = query + coin;
        int update = accountMapper.updateCoin(id, set, query);
        if (update <= 0) {
            throw new TccException();
        }
    }
}
