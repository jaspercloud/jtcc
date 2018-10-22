package com.jaspercloud.tcc.client.autoconfig;

import com.jaspercloud.tcc.client.protocol.DubboBeanCenterConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan({
        "com.jaspercloud.tcc.client.coordinator",
        "com.jaspercloud.tcc.client.aop",
        "com.jaspercloud.tcc.client.support"
})
@EnableConfigurationProperties(TccClientConfigProperties.class)
@ImportAutoConfiguration({
        TccAdvisorOrderPostProcessor.class,
        TccMethodPostProcessor.class,
        DubboBeanCenterConfig.class
})
@Configuration
public class TccClientAutoConfig {

}
