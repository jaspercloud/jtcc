package com.github.jaspercloud.tcc.dubbo.demo;

import com.github.jaspercloud.tcc.dubbo.demo.service.impl.OrderServiceImpl;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class AopTest implements MethodInterceptor {

    @Test
    public void test() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(OrderServiceImpl.class);
        enhancer.setCallback(this);
        Object object = enhancer.create();
        enhancer.createClass();
        OrderServiceImpl proxy1 = (OrderServiceImpl) object;
        proxy1.confirmOrder(null);
        Class<?> clazz = object.getClass();
        OrderServiceImpl proxy2 = (OrderServiceImpl) clazz.newInstance();
        proxy2.confirmOrder(null);
        System.out.println();


//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setTarget(new OrderServiceImpl());
//        proxyFactoryBean.setSingleton(true);
//        proxyFactoryBean.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
//        proxyFactoryBean.addAdvice(new MethodInterceptor() {
//            @Override
//            public Object invoke(MethodInvocation invocation) throws Throwable {
//                return null;
//            }
//        });
//        Object object = proxyFactoryBean.getObject();
//        OrderServiceImpl impl = (OrderServiceImpl) object;
//        impl.confirmOrder(null);
        System.out.println();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return null;
    }
}
