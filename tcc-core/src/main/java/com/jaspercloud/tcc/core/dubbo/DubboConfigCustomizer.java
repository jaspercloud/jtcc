package com.jaspercloud.tcc.core.dubbo;

public interface DubboConfigCustomizer<T> {

    void customize(T config);
}
