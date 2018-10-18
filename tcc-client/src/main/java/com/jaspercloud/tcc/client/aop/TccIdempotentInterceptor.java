package com.jaspercloud.tcc.client.aop;

import com.jaspercloud.tcc.client.support.idempotent.TccMethodLogDao;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TccIdempotentInterceptor implements MethodInterceptor {

    @Autowired
    protected TccMethodLogDao tccMethodLogDao;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!tccMethodLogDao.canTccTransaction()) {
            return null;
        }
        return invocation.proceed();
    }
}