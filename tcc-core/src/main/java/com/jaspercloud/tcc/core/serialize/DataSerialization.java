package com.jaspercloud.tcc.core.serialize;

public interface DataSerialization {

    byte[] serialize(Object obj) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> type) throws Exception;
}
