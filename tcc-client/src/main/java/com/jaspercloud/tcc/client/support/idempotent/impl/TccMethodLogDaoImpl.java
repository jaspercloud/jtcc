package com.jaspercloud.tcc.client.support.idempotent.impl;

import com.jaspercloud.tcc.client.support.TccMethodDaoSupport;
import com.jaspercloud.tcc.client.support.idempotent.TccMethodLogDao;
import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.util.TccConstants;
import com.jaspercloud.tcc.core.util.TccContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Repository
public class TccMethodLogDaoImpl extends TccMethodDaoSupport implements TccMethodLogDao {

    @Override
    public boolean canTccTransaction() {
        boolean execute = getTransactionTemplate().execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                    throw new TccException("Inactive transaction");
                }
                TccContext tccContext = TccContext.get();
                if (null == tccContext) {
                    throw new IllegalArgumentException("not found tccContext");
                }
                String tid = tccContext.getTid();
                String uniqueName = tccContext.getUniqueName();
                TccConstants.TccStatus tccStatus = tccContext.getTccStatus();
                if (TccConstants.TccStatus.Try.equals(tccStatus)) {
                    boolean hasTccLog = hasTccLog(tid, uniqueName);
                    if (hasTccLog) {
                        return false;
                    }
                    saveTccLog(tid, uniqueName);
                    return true;
                } else if (TccConstants.TccStatus.Confirm.equals(tccStatus)
                        || TccConstants.TccStatus.Cancel.equals(tccStatus)) {
                    boolean isCompleted = isCompleted(tid, uniqueName);
                    if (isCompleted) {
                        return false;
                    }
                    int update = updateTccStatus(tid, uniqueName, tccStatus);
                    if (update <= 0) {
                        return false;
                    }
                    return true;
                } else {
                    throw new UnsupportedOperationException(tccStatus.toString());
                }
            }
        });
        return execute;
    }

    private boolean hasTccLog(String tid, String methodName) {
        List<String> list = getTccLog(tid, methodName);
        if (!list.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isCompleted(String tid, String methodName) {
        List<String> list = getTccLog(tid, methodName);
        if (list.isEmpty()) {
            return true;
        }
        String tccStatus = list.get(0);
        if (TccConstants.TccStatus.Confirm.equals(tccStatus)
                || TccConstants.TccStatus.Cancel.equals(tccStatus)) {
            return true;
        }
        return false;
    }

    private void saveTccLog(String tid, String methodName) {
        String sql = "insert into tcc_log (tid,tcc_method,tcc_status) values (?,?,?)";
        Object[] args = {tid, methodName, TccConstants.TccStatus.Try.toString()};
        getJdbcTemplate().update(sql, args);
    }

    private List<String> getTccLog(String tid, String methodName) {
        String sql = "select tcc_status from tcc_log where tid=? and tcc_method=? limit 1";
        Object[] args = {tid, methodName};
        List<String> list = getJdbcTemplate().queryForList(sql, args, String.class);
        return list;
    }

    private int updateTccStatus(String tid, String methodName, TccConstants.TccStatus tccStatus) {
        String sql = "update tcc_log set tcc_status=? where tid=? and tcc_method=? and tcc_status=?";
        Object[] args = {tccStatus.toString(), tid, methodName, TccConstants.TccStatus.Try.toString()};
        int update = getJdbcTemplate().update(sql, args);
        return update;
    }
}
