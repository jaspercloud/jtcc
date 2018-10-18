package com.jaspercloud.tcc.client.autoconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class TccMethodRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Set<String> packagesToScan;

    public TccMethodRegistryPostProcessor(String... packagesToScan) {
        this(Arrays.asList(packagesToScan));
    }

    public TccMethodRegistryPostProcessor(Collection<String> packagesToScan) {
        this(new LinkedHashSet<>(packagesToScan));
    }

    public TccMethodRegistryPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        ClassPathTccMethodScanner scanner = new ClassPathTccMethodScanner(beanDefinitionRegistry);
        scanner.registerFilters();
        String[] packages = packagesToScan.toArray(new String[0]);
        scanner.scan(packages);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
}
