package com.jaspercloud.tcc.client.autoconfig;

import com.jaspercloud.tcc.core.config.TccConfigProperties;

public class TccClientConfigProperties extends TccConfigProperties {

    private String dbType;
    private long tccTimeout = 10 * 1000;

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public long getTccTimeout() {
        return tccTimeout;
    }

    public void setTccTimeout(long tccTimeout) {
        this.tccTimeout = tccTimeout;
    }
}
