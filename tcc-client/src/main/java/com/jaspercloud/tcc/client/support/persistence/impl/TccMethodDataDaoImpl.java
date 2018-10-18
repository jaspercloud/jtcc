package com.jaspercloud.tcc.client.support.persistence.impl;

import com.jaspercloud.tcc.client.support.TccMethodDaoSupport;
import com.jaspercloud.tcc.client.support.persistence.TccMethodDataDao;
import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.util.TccContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class TccMethodDataDaoImpl extends TccMethodDaoSupport implements TccMethodDataDao {

    @Override
    public void saveData(Map<String, String> map) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new TccException("Inactive transaction");
        }
        TccContext tccContext = TccContext.get();
        if (null == tccContext) {
            throw new IllegalArgumentException("not found tccContext");
        }
        String tid = tccContext.getTid();
        String sql = "insert into tcc_data (tid,tcc_key,tcc_value) values (?,?,?)";
        ArrayList<Map.Entry<String, String>> list = new ArrayList<>(map.entrySet());
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Map.Entry<String, String> entry = list.get(i);
                        String key = entry.getKey();
                        String value = entry.getValue();
                        ps.setString(1, tid);
                        ps.setString(2, key);
                        ps.setString(3, value);
                    }

                    @Override
                    public int getBatchSize() {
                        return map.size();
                    }
                });
            }
        });
    }

    @Override
    public Map<String, String> getData() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new TccException("Inactive transaction");
        }
        TccContext tccContext = TccContext.get();
        if (null == tccContext) {
            throw new IllegalArgumentException("not found tccContext");
        }
        String tid = tccContext.getTid();
        String sql = "select * from tcc_data where tid=?";
        Map<String, String> map = getTransactionTemplate().execute(new TransactionCallback<Map<String, String>>() {
            @Override
            public Map<String, String> doInTransaction(TransactionStatus status) {
                Map<String, String> map = getJdbcTemplate().query(sql, new Object[]{tid}, new ResultSetExtractor<Map<String, String>>() {
                    @Override
                    public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        Map<String, String> map = new HashMap<>();
                        while (rs.next()) {
                            String key = rs.getString("tcc_key");
                            String value = rs.getString("tcc_value");
                            map.put(key, value);
                        }
                        return map;
                    }
                });
                return map;
            }
        });
        return map;
    }

    @Override
    public void deleteData() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new TccException("Inactive transaction");
        }
        TccContext tccContext = TccContext.get();
        if (null == tccContext) {
            throw new IllegalArgumentException("not found tccContext");
        }
        String tid = tccContext.getTid();
        String sql = "delete from tcc_data where tid=?";
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                getJdbcTemplate().update(sql, new Object[]{tid});
            }
        });
    }
}
