package com.jaspercloud.tcc.core.coordinator;

public interface TccTransactionProcessor {

    void confirm(TccMethodData data) throws Exception;

    void cancel(TccMethodData data) throws Exception;
}
