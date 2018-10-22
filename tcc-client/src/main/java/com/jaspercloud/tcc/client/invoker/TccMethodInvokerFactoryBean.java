package com.jaspercloud.tcc.client.invoker;

import com.jaspercloud.tcc.client.autoconfig.TccClientConfigProperties;
import com.jaspercloud.tcc.client.support.TransactionManagerCache;
import com.jaspercloud.tcc.client.support.table.TccTableGenerator;
import com.jaspercloud.tcc.core.support.transaction.TccPlatformTransactionManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;

public class TccMethodInvokerFactoryBean implements FactoryBean<TccMethodInvoker>, InitializingBean, BeanFactoryAware {

    private String uniqueName;
    private Class<?> beanClass;
    private Method tryMethod;
    private Method confirmMethod;
    private Method cancelMethod;
    private String transactionManagerName;

    private BeanFactory beanFactory;
    private TccMethodInvoker tccMethodInvoker;

    public String getUniqueName() {
        return uniqueName;
    }

    public void setTryMethod(Method tryMethod) {
        this.tryMethod = tryMethod;
    }

    public void setConfirmMethod(Method confirmMethod) {
        this.confirmMethod = confirmMethod;
    }

    public void setCancelMethod(Method cancelMethod) {
        this.cancelMethod = cancelMethod;
    }

    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }

    public TccMethodInvokerFactoryBean(String uniqueName, Class<?> beanClass) {
        this.uniqueName = uniqueName;
        this.beanClass = beanClass;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Object target = beanFactory.getBean(beanClass);
        tccMethodInvoker = new TccMethodInvoker(uniqueName, target);
        tccMethodInvoker.setTryMethod(tryMethod);
        tccMethodInvoker.setConfirmMethod(confirmMethod);
        tccMethodInvoker.setCancelMethod(cancelMethod);

        TccClientConfigProperties config = beanFactory.getBean(TccClientConfigProperties.class);
        if (StringUtils.isNotEmpty(config.getDbType())) {
            TransactionManagerCache transactionManagerCache = beanFactory.getBean(TransactionManagerCache.class);
            TccPlatformTransactionManager transactionManager = transactionManagerCache.getTransactionManager(transactionManagerName);
            TccTableGenerator tableGenerator = beanFactory.getBean(config.getDbType(), TccTableGenerator.class);
            initTccTable(transactionManager, tableGenerator);
        }
    }

    private void initTccTable(TccPlatformTransactionManager transactionManager, TccTableGenerator tccTableGenerator) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(transactionManager.getDataSource());
        jdbcTemplate.afterPropertiesSet();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                tccTableGenerator.createTccLogTable(jdbcTemplate);
            }
        });
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                tccTableGenerator.createTccDataTable(jdbcTemplate);
            }
        });
    }

    @Override
    public TccMethodInvoker getObject() throws Exception {
        return tccMethodInvoker;
    }

    @Override
    public Class<?> getObjectType() {
        return TccMethodInvoker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
