package com.japsercloud.tcc.server.repository;

import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.util.TccConstants;

import java.util.List;
import java.util.Map;

public interface TccTransactionRepository {

    String createTid(long timeout);

    boolean hasTid(String tid);

    void saveTccMethod(TccMethodData tccMethodData);

    Map<Long, TccMethodData> getTccMethodList(String tid);

    void deleteTccMethod(String tid, long methodId);

    List<TccTransactionInfo> getTccTransactionList(Long cursor, int limit);

    void updateTccTransactionStatus(String tid, TccConstants.TccStatus tccStatus);

    void deleteTccTransaction(String tid);
}
