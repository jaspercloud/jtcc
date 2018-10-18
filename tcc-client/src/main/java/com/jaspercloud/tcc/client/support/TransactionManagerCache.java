package com.jaspercloud.tcc.client.support;

import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.support.transaction.TccPlatformTransactionManager;
import com.jaspercloud.tcc.core.support.transaction.TccProxyTransactionManager;
import com.jaspercloud.tcc.core.util.ObjectCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionManagerCache implements ApplicationContextAware, InitializingBean {

    private Map<String, TccPlatformTransactionManager> beanNameTransactionManagerMap = new ConcurrentHashMap<>();
    private Map<Method, TccPlatformTransactionManager> methodTransactionManagerMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        PlatformTransactionManager defaultPlatformTransactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        Map<String, PlatformTransactionManager> beans = applicationContext.getBeansOfType(PlatformTransactionManager.class);
        beans.put("", defaultPlatformTransactionManager);
        for (Map.Entry<String, PlatformTransactionManager> entry : beans.entrySet()) {
            String beanName = entry.getKey();
            PlatformTransactionManager platformTransactionManager = entry.getValue();
            TccPlatformTransactionManager tccPlatformTransactionManager = null;
            if (DataSourceTransactionManager.class.isInstance(platformTransactionManager)) {
                DataSourceTransactionManager dataSourceTransactionManager = (DataSourceTransactionManager) platformTransactionManager;
                tccPlatformTransactionManager = new TccProxyTransactionManager(dataSourceTransactionManager);
            } else if (TccPlatformTransactionManager.class.isInstance(platformTransactionManager)) {
                tccPlatformTransactionManager = (TccPlatformTransactionManager) platformTransactionManager;
            }
            if (null != tccPlatformTransactionManager) {
                beanNameTransactionManagerMap.put(beanName, tccPlatformTransactionManager);
                String[] aliasesArray = applicationContext.getAliases(beanName);
                for (String aliases : aliasesArray) {
                    beanNameTransactionManagerMap.put(aliases, tccPlatformTransactionManager);
                }
            }
        }
    }

    public TccPlatformTransactionManager getTransactionManager(Method method) throws Exception {
        TccPlatformTransactionManager transactionManager = ObjectCache.get(methodTransactionManagerMap, method, new ObjectCache.Callback<TccPlatformTransactionManager>() {
            @Override
            public TccPlatformTransactionManager call() throws Exception {
                Transactional transactional = method.getAnnotation(Transactional.class);
                if (null == transactional) {
                    transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
                }
                if (null == transactional) {
                    throw new TccException("not found org.springframework.transaction.annotation.Transactional");
                }
                String beanName = transactional.value();
                if (StringUtils.isEmpty(beanName)) {
                    beanName = transactional.transactionManager();
                }
                TccPlatformTransactionManager transactionManager = beanNameTransactionManagerMap.get(beanName);
                if (null == transactionManager) {
                    throw new TccException("not found TccPlatformTransactionManager");
                }
                return transactionManager;
            }
        });
        return transactionManager;
    }

    public TccPlatformTransactionManager getTransactionManager(String beanName) throws Exception {
        TccPlatformTransactionManager transactionManager = beanNameTransactionManagerMap.get(beanName);
        if (null == transactionManager) {
            throw new TccException("not found TccPlatformTransactionManager");
        }
        return transactionManager;
    }
}
