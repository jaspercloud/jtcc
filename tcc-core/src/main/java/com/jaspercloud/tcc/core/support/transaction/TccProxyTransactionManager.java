package com.jaspercloud.tcc.core.support.transaction;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;

public class TccProxyTransactionManager implements TccPlatformTransactionManager {

    private DataSourceTransactionManager transactionManager;

    public TccProxyTransactionManager(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public DataSource getDataSource() {
        return transactionManager.getDataSource();
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return transactionManager.getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        transactionManager.commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        transactionManager.rollback(status);
    }
}
