package com.jaspercloud.tcc.core.support.transaction;

import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public interface TccPlatformTransactionManager extends PlatformTransactionManager {

    DataSource getDataSource();
}
