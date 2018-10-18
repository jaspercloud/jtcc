package com.github.jaspercloud.tcc.demo.account.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.jaspercloud.tcc.demo.account.dao.AccountMapper;
import com.github.jasperlcoud.tcc.demo.api.service.AccountService;
import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.support.persistence.TccMethodDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service(interfaceClass = AccountService.class)
@Transactional(transactionManager = "account")
public class AccountServiceImpl implements AccountService {

    @Autowired
    protected AccountMapper accountMapper;

    @Autowired
    protected TccMethodDataDao tccMethodDataDao;

    @TccMethod(uniqueName = "subCoin", confirmMethod = "confirmSubCoin", cancelMethod = "cancelSubCoin")
    @Override
    public void subCoin(long id, int coin) {
        Integer query = accountMapper.selectCoinById(id);
        if (query < coin) {
            throw new RuntimeException("余额不足");
        }
        int set = query - coin;
        int update = accountMapper.updateCoin(id, set, query);
        if (update <= 0) {
            throw new RuntimeException();
        }
    }

    public void confirmSubCoin(long id, int coin) {
    }

    public void cancelSubCoin(long id, int coin) {
        Integer query = accountMapper.selectCoinById(id);
        int set = query + coin;
        int update = accountMapper.updateCoin(id, set, query);
        if (update <= 0) {
            throw new RuntimeException();
        }
    }
}
