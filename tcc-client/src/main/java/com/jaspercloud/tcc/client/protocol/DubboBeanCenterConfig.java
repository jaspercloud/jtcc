package com.jaspercloud.tcc.client.protocol;

import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.jaspercloud.tcc.client.coordinator.LocalTccTransactionProcessor;
import com.jaspercloud.tcc.core.coordinator.TccTransactionCoordinator;
import com.jaspercloud.tcc.core.coordinator.TccTransactionProcessor;
import com.jaspercloud.tcc.core.dubbo.DubboBeanCenter;
import com.jaspercloud.tcc.core.dubbo.DubboConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class DubboBeanCenterConfig {

    @Bean
    public DubboBeanCenter dubboBeanCenter() {
        DubboBeanCenter dubboBeanCenter = new DubboBeanCenter();
        dubboBeanCenter.addServiceConfig(TccTransactionProcessor.class, LocalTccTransactionProcessor.class);
        dubboBeanCenter.addReferenceConfig(TccTransactionCoordinator.class).setCustomizer(new DubboConfigCustomizer<ReferenceBean>() {
            @Override
            public void customize(ReferenceBean config) {
                config.setMethods(new ArrayList<>());
                {
                    MethodConfig methodConfig = new MethodConfig();
                    methodConfig.setName("confirm");
                    methodConfig.setReturn(false);
                    config.getMethods().add(methodConfig);
                }
                {
                    MethodConfig methodConfig = new MethodConfig();
                    methodConfig.setName("cancel");
                    methodConfig.setReturn(false);
                    config.getMethods().add(methodConfig);
                }
            }
        });
        return dubboBeanCenter;
    }
}