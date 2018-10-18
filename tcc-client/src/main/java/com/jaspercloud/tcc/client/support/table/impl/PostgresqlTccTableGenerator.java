package com.jaspercloud.tcc.client.support.table.impl;

import com.jaspercloud.tcc.client.support.table.TccTableGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("postgresql")
public class PostgresqlTccTableGenerator implements TccTableGenerator {

    @Override
    public void createTccLogTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("CREATE SEQUENCE IF NOT EXISTS seq_tcc_log");
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS tcc_log (" +
                "id int8 NOT NULL DEFAULT nextval('seq_tcc_log')," +
                "tid varchar(128)," +
                "tcc_method varchar(128)," +
                "tcc_status varchar(16)," +
                "create_time timestamp DEFAULT now()," +
                "CONSTRAINT tcc_log_pkey PRIMARY KEY (id))");
        jdbcTemplate.update("CREATE INDEX IF NOT EXISTS tcc_log_tcc_method_idx ON tcc_log USING btree (tcc_method)");
        jdbcTemplate.update("CREATE INDEX IF NOT EXISTS tcc_log_tid_idx ON tcc_log USING btree (tid)");
    }

    @Override
    public void createTccDataTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("CREATE SEQUENCE IF NOT EXISTS seq_tcc_data");
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS tcc_data (" +
                "id int8 NOT NULL DEFAULT nextval('seq_tcc_data')," +
                "tid varchar(128)," +
                "tcc_key varchar(255)," +
                "tcc_value varchar(255)," +
                "CONSTRAINT tcc_data_pkey PRIMARY KEY (id))");
        jdbcTemplate.update("CREATE INDEX IF NOT EXISTS tcc_data_tid_idx ON tcc_data USING btree (tid)");
    }
}
