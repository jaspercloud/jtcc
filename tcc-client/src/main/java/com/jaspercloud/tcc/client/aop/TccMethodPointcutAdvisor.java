package com.jaspercloud.tcc.client.aop;

import com.jaspercloud.tcc.client.util.TccMethodCache;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class TccMethodPointcutAdvisor extends AbstractPointcutAdvisor {

    private StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            if (TccMethodCache.isTccMethod(method)) {
                return true;
            }
            return false;
        }
    };

    @Autowired
    private TccMethodInterceptor tccMethodInterceptor;

    public TccMethodPointcutAdvisor() {
        setOrder(-1);
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return tccMethodInterceptor;
    }
}
