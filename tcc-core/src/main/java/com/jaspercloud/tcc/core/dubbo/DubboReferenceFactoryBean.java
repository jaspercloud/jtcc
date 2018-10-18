package com.jaspercloud.tcc.core.dubbo;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.jaspercloud.tcc.core.config.TccConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DubboReferenceFactoryBean implements FactoryBean, InitializingBean, DisposableBean, ApplicationContextAware, BeanFactoryAware {

    private Class<?> interfaceClass;
    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;
    private ReferenceBean referenceBean;
    private DubboConfigCustomizer<ReferenceBean> customizer;

    public DubboConfigCustomizer<ReferenceBean> getCustomizer() {
        return customizer;
    }

    public void setCustomizer(DubboConfigCustomizer<ReferenceBean> customizer) {
        this.customizer = customizer;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public DubboReferenceFactoryBean(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
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
    public void afterPropertiesSet() throws Exception {
        TccConfigProperties config = beanFactory.getBean(TccConfigProperties.class);
        referenceBean = new ReferenceBean<>();
        referenceBean.setApplication(config.getApplication());
        referenceBean.setRegistry(config.getRegistry());
        referenceBean.setConsumer(config.getConsumer());
        referenceBean.setInterface(interfaceClass);
        referenceBean.setApplicationContext(applicationContext);
        if (null != customizer) {
            customizer.customize(referenceBean);
        }
        referenceBean.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        referenceBean.destroy();
    }

    @Override
    public Object getObject() throws Exception {
        return referenceBean.getObject();
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
