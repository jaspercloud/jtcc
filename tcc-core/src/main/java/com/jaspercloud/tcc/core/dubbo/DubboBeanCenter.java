package com.jaspercloud.tcc.core.dubbo;

import com.jaspercloud.tcc.core.util.BeanNameUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.ArrayList;
import java.util.List;

public class DubboBeanCenter implements BeanDefinitionRegistryPostProcessor {

    private List<DubboServiceFactoryBean> serviceConfigList = new ArrayList<>();
    private List<DubboReferenceFactoryBean> referenceConfigList = new ArrayList<>();

    public DubboServiceFactoryBean addServiceConfig(Class<?> interfaceClass, Class<?> refClass) {
        DubboServiceFactoryBean dubboServiceFactoryBean = new DubboServiceFactoryBean(interfaceClass, refClass);
        serviceConfigList.add(dubboServiceFactoryBean);
        return dubboServiceFactoryBean;
    }

    public DubboReferenceFactoryBean addReferenceConfig(Class<?> interfaceClass) {
        DubboReferenceFactoryBean dubboReferenceFactoryBean = new DubboReferenceFactoryBean(interfaceClass);
        referenceConfigList.add(dubboReferenceFactoryBean);
        return dubboReferenceFactoryBean;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (DubboServiceFactoryBean bean : serviceConfigList) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DubboServiceFactoryBean.class);
            builder.addConstructorArgValue(bean.getInterfaceClass());
            builder.addConstructorArgValue(bean.getRefClass());
            builder.addPropertyValue("customizer", bean.getCustomizer());
            String beanName = BeanNameUtil.generateBeanName(bean.getInterfaceClass()) + DubboServiceFactoryBean.class.getSimpleName();
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setPrimary(true);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
        for (DubboReferenceFactoryBean bean : referenceConfigList) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DubboReferenceFactoryBean.class);
            builder.addConstructorArgValue(bean.getInterfaceClass());
            builder.addPropertyValue("customizer", bean.getCustomizer());
            String beanName = BeanNameUtil.generateBeanName(bean.getInterfaceClass()) + DubboReferenceFactoryBean.class.getSimpleName();
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setPrimary(true);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
