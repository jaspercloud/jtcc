package com.jaspercloud.tcc.client.support.idempotent;

public interface TccMethodLogDao {

    boolean canTccTransaction();
}
