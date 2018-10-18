package com.japsercloud.tcc.server.repository.impl;

import com.japsercloud.tcc.server.repository.TccTransactionInfo;
import com.japsercloud.tcc.server.repository.TccTransactionRepository;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.util.ProtobufUtil;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.Base64Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class JdbcTccTransactionRepository implements TccTransactionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String createTid(long timeout) {
        String tid = UUID.randomUUID().toString();
        return tid;
    }

    @Override
    public boolean hasTid(String tid) {
        try {
            String sql = "select tid from tcc_repository where tid=? limit 1";
            String result = jdbcTemplate.queryForObject(sql, new Object[]{tid}, String.class);
            if (StringUtils.isNotEmpty(result)) {
                return true;
            } else {
                return false;
            }
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void saveTccMethod(TccMethodData tccMethodData) {
        byte[] bytes = ProtobufUtil.toByteArray(tccMethodData);
        String base64 = Base64Utils.encodeToString(bytes);
        String sql = "insert into tcc_methods (tid,method_data) values (?,?)";
        jdbcTemplate.update(sql, new Object[]{tccMethodData.getTid(), base64});
    }

    @Override
    public Map<Long, TccMethodData> getTccMethodList(String tid) {
        String sql = "select * from tcc_methods where tid=?";
        Map<Long, String> map = jdbcTemplate.query(sql, new Object[]{tid}, new ResultSetExtractor<Map<Long, String>>() {
            @Override
            public Map<Long, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Long, String> map = new HashMap<>();
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String methodData = rs.getString("method_data");
                    map.put(id, methodData);
                }
                return map;
            }
        });
        Map<Long, TccMethodData> dataMap = new HashMap<>();
        map.entrySet().forEach(new Consumer<Map.Entry<Long, String>>() {
            @Override
            public void accept(Map.Entry<Long, String> entry) {
                Long key = entry.getKey();
                String value = entry.getValue();
                byte[] bytes = Base64Utils.decodeFromString(value);
                TccMethodData tccMethodData = ProtobufUtil.mergeFrom(bytes, new TccMethodData());
                dataMap.put(key, tccMethodData);
            }
        });
        return dataMap;
    }

    @Override
    public void deleteTccMethod(String tid, long methodId) {
        String sql = "delete from tcc_methods where id=? and tid=?";
        jdbcTemplate.update(sql, new Object[]{methodId, tid});
    }

    @Override
    public List<TccTransactionInfo> getTccTransactionList(Long cursor, int limit) {
        String sql = "select * from tcc_repository id>? order by id limit ?";
        List<TccTransactionInfo> list = jdbcTemplate.query(sql, new Object[]{cursor, limit}, new ResultSetExtractor<List<TccTransactionInfo>>() {
            @Override
            public List<TccTransactionInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<TccTransactionInfo> list = new ArrayList<>();
                while (rs.next()) {
                    TccTransactionInfo info = new TccTransactionInfo();
                    info.setId(rs.getLong("id"));
                    info.setTid(rs.getString("tid"));
                    info.setCreateTime(rs.getDate("create_time").getTime());
                    info.setTimeout(rs.getLong("timeout"));
                    info.setTccStatus(rs.getString("tcc_status"));
                    info.setTccTimeoutMillis(rs.getLong("timeout_millis"));
                    info.setCompensateTimeoutMillis(rs.getLong("compensate_millis"));
                    list.add(info);
                }
                return list;
            }
        });
        return list;
    }

    @Override
    public void updateTccTransactionStatus(String tid, TccConstants.TccStatus tccStatus) {
        String sql = "update tcc_repository set tcc_status=? where tid=?";
        jdbcTemplate.update(sql, new Object[]{tccStatus.toString(), tid});
    }

    @Override
    public void deleteTccTransaction(String tid) {
        String sql = "delete from tcc_repository tid=?";
        jdbcTemplate.update(sql, new Object[]{tid});
    }
}
