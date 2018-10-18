package com.jaspercloud.tcc.core.coordinator;

public interface TccTransactionCoordinator {

    String createTid(long timeout) throws Exception;

    void joinTccMethod(TccMethodData data) throws Exception;

    void confirm(String tid) throws Exception;

    void cancel(String tid) throws Exception;
}
