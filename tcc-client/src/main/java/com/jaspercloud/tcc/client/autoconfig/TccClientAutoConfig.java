package com.jaspercloud.tcc.client.autoconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

@ComponentScan({
        "com.jaspercloud.tcc.client.coordinator",
        "com.jaspercloud.tcc.client.aop",
        "com.jaspercloud.tcc.client.support"
})
@EnableConfigurationProperties(TccClientConfigProperties.class)
@ImportAutoConfiguration({DubboBeanCenterConfig.class})
@Configuration
public class TccClientAutoConfig {

    @Bean
    public TccAdvisorOrderPostProcessor tccAdvisorOrderPostProcessor() {
        return new TccAdvisorOrderPostProcessor();
    }

    @Configuration
    @Import({AutoConfiguredTccMethodScannerRegistrar.class})
    public static class TccMethodScannerRegistrarNotFoundConfiguration {

    }

    public static class AutoConfiguredTccMethodScannerRegistrar implements BeanFactoryAware, ResourceLoaderAware, ImportBeanDefinitionRegistrar {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            ClassPathTccMethodScanner scanner = new ClassPathTccMethodScanner(registry);
            scanner.registerFilters();
            List<String> packages = AutoConfigurationPackages.get(beanFactory);
            scanner.scan(packages.toArray(new String[0]));
        }
    }
}
