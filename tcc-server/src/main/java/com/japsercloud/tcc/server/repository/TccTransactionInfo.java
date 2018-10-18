package com.japsercloud.tcc.server.repository;

public class TccTransactionInfo {

    private Long id;
    private String tid;
    private Long createTime;
    private Long timeout;
    private String tccStatus;
    private Long tccTimeoutMillis;
    private Long compensateTimeoutMillis;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public String getTccStatus() {
        return tccStatus;
    }

    public void setTccStatus(String tccStatus) {
        this.tccStatus = tccStatus;
    }

    public Long getTccTimeoutMillis() {
        return tccTimeoutMillis;
    }

    public void setTccTimeoutMillis(Long tccTimeoutMillis) {
        this.tccTimeoutMillis = tccTimeoutMillis;
    }

    public Long getCompensateTimeoutMillis() {
        return compensateTimeoutMillis;
    }

    public void setCompensateTimeoutMillis(Long compensateTimeoutMillis) {
        this.compensateTimeoutMillis = compensateTimeoutMillis;
    }

    public TccTransactionInfo() {
    }
}
