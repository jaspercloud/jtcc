package com.jaspercloud.tcc.core.config;

import com.alibaba.dubbo.config.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(TccConfigProperties.PREFIX)
public class TccConfigProperties {

    public static final String PREFIX = "jaspercloud.tcc";

    private ApplicationConfig application = new ApplicationConfig();
    private RegistryConfig registry = new RegistryConfig();
    private ProtocolConfig protocol = new ProtocolConfig();
    private ProviderConfig provider = new ProviderConfig();
    private ConsumerConfig consumer = new ConsumerConfig();

    public TccConfigProperties() {
        application.setQosEnable(false);
        consumer.setCheck(false);
    }

    public ApplicationConfig getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfig application) {
        this.application = application;
    }

    public RegistryConfig getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryConfig registry) {
        this.registry = registry;
    }

    public ProtocolConfig getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolConfig protocol) {
        this.protocol = protocol;
    }

    public ProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public ConsumerConfig getConsumer() {
        return consumer;
    }

    public void setConsumer(ConsumerConfig consumer) {
        this.consumer = consumer;
    }
}
