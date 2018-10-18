package com.jaspercloud.tcc.client.support.table;

import org.springframework.jdbc.core.JdbcTemplate;

public interface TccTableGenerator {

    void createTccLogTable(JdbcTemplate jdbcTemplate);

    void createTccDataTable(JdbcTemplate jdbcTemplate);
}
