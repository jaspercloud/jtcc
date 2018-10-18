package com.jaspercloud.tcc.client.autoconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

public class TccAdvisorOrderPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements PriorityOrdered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (BeanFactoryTransactionAttributeSourceAdvisor.class.isInstance(bean)) {
            BeanFactoryTransactionAttributeSourceAdvisor advisor = (BeanFactoryTransactionAttributeSourceAdvisor) bean;
            advisor.setOrder(0);
            return bean;
        } else {
            return super.postProcessAfterInitialization(bean, beanName);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
