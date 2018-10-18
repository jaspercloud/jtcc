package com.jaspercloud.tcc.core.coordinator;

import com.jaspercloud.tcc.core.exception.SerializationException;
import com.jaspercloud.tcc.core.serialize.DataSerialization;
import com.jaspercloud.tcc.core.serialize.DefaultDataSerialization;
import com.jaspercloud.tcc.core.util.MethodUtil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TccMethodData implements Serializable {

    private String applicationName;
    private String uniqueName;
    private String tid;
    private String[] argsTypes;
    private byte[][] argsBytes;

    public String getApplicationName() {
        return applicationName;
    }

    public String getTid() {
        return tid;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String[] getArgsTypes() {
        return argsTypes;
    }

    public byte[][] getArgsBytes() {
        return argsBytes;
    }

    public Object[] getArgs() throws Exception {
        Class<?>[] argsTypes = MethodUtil.decodeArgsTypes(getArgsTypes());
        byte[][] argsBytes = getArgsBytes();
        List<Object> list = new ArrayList<>();
        DataSerialization dataSerialization = new DefaultDataSerialization();
        for (int i = 0; i < argsTypes.length; i++) {
            Class<?> type = argsTypes[i];
            byte[] bytes = argsBytes[i];
            Object arg = dataSerialization.deserialize(bytes, type);
            list.add(arg);
        }
        Object[] args = list.toArray();
        return args;
    }

    public TccMethodData() {
    }

    public TccMethodData(String applicationName, String uniqueName, String tid, Method method, Object[] args) {
        this.applicationName = applicationName;
        this.uniqueName = uniqueName;
        this.tid = tid;
        Class<?>[] parameterTypes = method.getParameterTypes();
        this.argsTypes = MethodUtil.encodeArgsTypes(parameterTypes);
        DataSerialization dataSerialization = new DefaultDataSerialization();
        List<byte[]> argList = Arrays.asList(args).stream().map(new Function<Object, byte[]>() {
            @Override
            public byte[] apply(Object o) {
                try {
                    byte[] bytes = dataSerialization.serialize(o);
                    return bytes;
                } catch (Exception e) {
                    throw new SerializationException(e.getMessage(), e);
                }
            }
        }).collect(Collectors.toList());
        this.argsBytes = argList.toArray(new byte[0][]);
    }
}
