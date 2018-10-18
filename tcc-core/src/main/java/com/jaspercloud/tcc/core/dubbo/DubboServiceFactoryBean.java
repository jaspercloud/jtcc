package com.jaspercloud.tcc.core.dubbo;

import com.alibaba.dubbo.config.spring.ServiceBean;
import com.jaspercloud.tcc.core.config.TccConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class DubboServiceFactoryBean implements FactoryBean<ServiceBean>, InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, BeanFactoryAware, BeanNameAware {

    private Class<?> interfaceClass;
    private Class<?> refClass;
    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;
    private String beanName;
    private ServiceBean serviceBean;
    private DubboConfigCustomizer<ServiceBean> customizer;

    public DubboConfigCustomizer<ServiceBean> getCustomizer() {
        return customizer;
    }

    public void setCustomizer(DubboConfigCustomizer<ServiceBean> customizer) {
        this.customizer = customizer;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Class<?> getRefClass() {
        return refClass;
    }

    public DubboServiceFactoryBean(Class<?> interfaceClass, Class<?> refClass) {
        this.interfaceClass = interfaceClass;
        this.refClass = refClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TccConfigProperties config = beanFactory.getBean(TccConfigProperties.class);
        serviceBean = new ServiceBean<>();
        serviceBean.setBeanName(beanName);
        serviceBean.setApplication(config.getApplication());
        serviceBean.setRegistry(config.getRegistry());
        serviceBean.setProvider(config.getProvider());
        serviceBean.setProtocol(config.getProtocol());
        serviceBean.setInterface(interfaceClass);
        serviceBean.setApplicationContext(applicationContext);
        Object ref = beanFactory.getBean(refClass);
        serviceBean.setRef(ref);
        if (null != customizer) {
            customizer.customize(serviceBean);
        }
    }

    @Override
    public void destroy() throws Exception {
        serviceBean.destroy();
    }

    @Override
    public ServiceBean getObject() throws Exception {
        return serviceBean;
    }

    @Override
    public Class<?> getObjectType() {
        return ServiceBean.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        serviceBean.onApplicationEvent(event);
    }
}
