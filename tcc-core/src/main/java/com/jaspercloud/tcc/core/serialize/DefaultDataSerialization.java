package com.jaspercloud.tcc.core.serialize;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.hessian2.Hessian2ObjectInput;
import com.alibaba.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DefaultDataSerialization implements DataSerialization {

    @Override
    public byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutput serialize = new Hessian2ObjectOutput(stream);
        serialize.writeObject(obj);
        serialize.flushBuffer();
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectInput deserialize = new Hessian2ObjectInput(stream);
        T object = deserialize.readObject(type);
        return object;
    }
}
