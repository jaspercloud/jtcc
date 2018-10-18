package com.jaspercloud.tcc.client.aop;

import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.autoconfig.TccClientConfigProperties;
import com.jaspercloud.tcc.client.support.TransactionManagerCache;
import com.jaspercloud.tcc.client.util.TccMethodCache;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.coordinator.TccTransactionCoordinator;
import com.jaspercloud.tcc.core.support.transaction.TccPlatformTransactionManager;
import com.jaspercloud.tcc.core.util.TccConstants;
import com.jaspercloud.tcc.core.util.TccContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;

@Component
public class TccMethodInterceptor implements MethodInterceptor, InitializingBean, Serializable {

    @Autowired
    private TccTransactionCoordinator tccTransactionCoordinator;

    @Autowired
    private TransactionManagerCache transactionManagerCache;

    @Autowired
    private TccClientConfigProperties config;

    private String appName;

    @Override
    public void afterPropertiesSet() {
        appName = config.getApplication().getName();
        if (StringUtils.isEmpty(appName)) {
            throw new NullPointerException("appName is null");
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        TccMethod tccMethod = TccMethodCache.getTccAnnotation(method);
        Object result;
        if (null != tccMethod) {
            result = invokeTryMethod(tccMethod, invocation);
        } else {
            result = invokeCompleteMethod(invocation);
        }
        return result;
    }

    private Object invokeTryMethod(TccMethod tccMethod, MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        TccPlatformTransactionManager transactionManager = transactionManagerCache.getTransactionManager(method);
        try {
            TccContext tccContext = TccContext.get();
            if (null == tccContext) {
                //tcc事务发起人
                String tid = tccTransactionCoordinator.createTid(tccMethod.timeout());
                tccContext = TccContext.create(tid, true);
            }
            String tid = tccContext.getTid();
            tccContext.setTccStatus(TccConstants.TccStatus.Try);
            joinTccMethod(tid, tccMethod, invocation);

            try {
                Object result;
                try {
                    TccContext joinTccContext = TccContext.joinTccContext(tccContext);
                    joinTccContext.setUniqueName(tccMethod.uniqueName());
                    joinTccContext.setTccStatus(tccContext.getTccStatus());
                    joinTccContext.setTransactionManager(transactionManager);
                    result = invocation.proceed();
                } finally {
                    TccContext.release();
                }
                if (tccContext.isOriginator()) {
                    tccTransactionCoordinator.confirm(tid);
                }
                return result;
            } catch (Exception e) {
                if (tccContext.isOriginator()) {
                    tccTransactionCoordinator.cancel(tid);
                }
                throw e;
            }
        } finally {
            TccContext tccContext = TccContext.get();
            if (null != tccContext && tccContext.isOriginator()) {
                TccContext.clean();
            }
        }
    }

    private Object invokeCompleteMethod(MethodInvocation invocation) throws Throwable {
        TccContext tccContext = TccContext.get();
        Method method = invocation.getMethod();
        TccPlatformTransactionManager transactionManager = transactionManagerCache.getTransactionManager(method);
        try {
            TccContext joinTccContext = TccContext.joinTccContext(tccContext);
            joinTccContext.setUniqueName(tccContext.getUniqueName());
            joinTccContext.setTccStatus(tccContext.getTccStatus());
            joinTccContext.setTransactionManager(transactionManager);
            Object result = invocation.proceed();
            return result;
        } finally {
            TccContext.release();
        }
    }

    private void joinTccMethod(String tid, TccMethod tccMethod, MethodInvocation invocation) throws Exception {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        TccMethodData tccMethodData = new TccMethodData(appName, tccMethod.uniqueName(), tid, method, args);
        tccTransactionCoordinator.joinTccMethod(tccMethodData);
    }
}
