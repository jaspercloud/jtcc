package com.jaspercloud.tcc.core.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by TimoRD on 2017/12/20.
 */
public final class ProtobufUtil {

    private ProtobufUtil() {

    }

    public static <T> T mergeFrom(byte[] bytes, T obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        ProtobufIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    public static byte[] toByteArray(Object obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        byte[] bytes = ProtobufIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate());
        return bytes;
    }
}
