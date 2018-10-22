package com.jaspercloud.tcc.client.autoconfig;

import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.coordinator.TccMethodInvokerFactory;
import com.jaspercloud.tcc.client.invoker.TccMethodInvoker;
import com.jaspercloud.tcc.client.invoker.TccMethodInvokerFactoryBean;
import com.jaspercloud.tcc.client.util.TccMethodCache;
import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Configuration
public class TccMethodPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean, BeanFactoryAware, PriorityOrdered {

    private BeanFactory beanFactory;
    private TccMethodInvokerFactory tccMethodInvokerFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        tccMethodInvokerFactory = beanFactory.getBean(TccMethodInvokerFactory.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = AopUtils.getTargetClass(bean);
        Transactional classTransactional = AnnotationUtils.findAnnotation(clazz, Transactional.class);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            TccMethod tccMethod = AnnotationUtils.findAnnotation(method, TccMethod.class);
            if (null == tccMethod) {
                continue;
            }
            Transactional methodTransactional = AnnotationUtils.findAnnotation(method, Transactional.class);
            if (null == methodTransactional && null == classTransactional) {
                throw new TccException("not found org.springframework.transaction.annotation.Transactional");
            }
            Transactional transactional = methodTransactional;
            if (null == transactional) {
                transactional = classTransactional;
            }
            String transactionManager = transactional.transactionManager();
            if (StringUtils.isEmpty(transactionManager)) {
                transactionManager = transactional.value();
            }
            try {
                String uniqueName = tccMethod.uniqueName();
                Method tryMethod = method;
                Method confirmMethod = clazz.getMethod(tccMethod.confirmMethod(), tryMethod.getParameterTypes());
                Method cancelMethod = clazz.getMethod(tccMethod.cancelMethod(), tryMethod.getParameterTypes());
                TccMethodCache.addTccMethod(tryMethod, TccConstants.TccStatus.Try);
                TccMethodCache.addTccMethod(confirmMethod, TccConstants.TccStatus.Confirm);
                TccMethodCache.addTccMethod(cancelMethod, TccConstants.TccStatus.Cancel);
                TccMethodInvokerFactoryBean factoryBean = new TccMethodInvokerFactoryBean(uniqueName, clazz);
                factoryBean.setTryMethod(tryMethod);
                factoryBean.setConfirmMethod(confirmMethod);
                factoryBean.setCancelMethod(cancelMethod);
                factoryBean.setTransactionManagerName(transactionManager);
                factoryBean.setBeanFactory(beanFactory);
                factoryBean.afterPropertiesSet();
                TccMethodInvoker methodInvoker = factoryBean.getObject();
                tccMethodInvokerFactory.addTccMethodInvoker(methodInvoker);
            } catch (Exception e) {
                throw new BeanCreationException(e.getMessage(), e);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
