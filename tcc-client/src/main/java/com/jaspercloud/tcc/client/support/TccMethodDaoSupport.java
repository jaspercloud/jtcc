package com.jaspercloud.tcc.client.support;

import com.jaspercloud.tcc.core.support.transaction.TccPlatformTransactionManager;
import com.jaspercloud.tcc.core.util.TccContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

public class TccMethodDaoSupport implements InitializingBean {

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                TccContext tccContext = TccContext.get();
                TccPlatformTransactionManager transactionManager = tccContext.getTransactionManager();
                DataSource dataSource = transactionManager.getDataSource();
                return dataSource;
            }

            @Override
            public void afterPropertiesSet() {
            }
        };
        transactionTemplate = new TransactionTemplate(new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                TccContext tccContext = TccContext.get();
                TccPlatformTransactionManager transactionManager = tccContext.getTransactionManager();
                TransactionStatus transaction = transactionManager.getTransaction(definition);
                return transaction;
            }

            @Override
            public void commit(TransactionStatus status) throws TransactionException {
                TccContext tccContext = TccContext.get();
                TccPlatformTransactionManager transactionManager = tccContext.getTransactionManager();
                transactionManager.commit(status);
            }

            @Override
            public void rollback(TransactionStatus status) throws TransactionException {
                TccContext tccContext = TccContext.get();
                TccPlatformTransactionManager transactionManager = tccContext.getTransactionManager();
                transactionManager.rollback(status);
            }
        });
        transactionTemplate.afterPropertiesSet();
    }
}
