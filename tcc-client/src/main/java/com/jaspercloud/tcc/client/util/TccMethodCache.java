package com.jaspercloud.tcc.client.util;

import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.core.util.MethodUtil;
import com.jaspercloud.tcc.core.util.ObjectCache;
import com.jaspercloud.tcc.core.util.TccConstants;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TccMethodCache {

    private static Map<String, TccConstants.TccStatus> tccMethodMap = new ConcurrentHashMap<>();
    private static Map<String, TccMethod> tccAnnotationMap = new ConcurrentHashMap<>();

    private TccMethodCache() {

    }

    public static void addTccMethod(Method method, TccConstants.TccStatus tccStatus) {
        String methodSign = MethodUtil.getMethodSign(method);
        tccMethodMap.put(methodSign, tccStatus);
    }

    /**
     * 使用字符串判断，Method可是被Cglib代理
     *
     * @param method
     * @return
     */
    public static TccConstants.TccStatus getMethodTccStatus(Method method) {
        String methodSign = MethodUtil.getMethodSign(method);
        TccConstants.TccStatus tccStatus = tccMethodMap.get(methodSign);
        return tccStatus;
    }

    public static boolean isTccMethod(Method method) {
        if (null != getMethodTccStatus(method)) {
            return true;
        }
        return false;
    }

    public static TccMethod getTccAnnotation(Method method) throws Exception {
        String methodSign = MethodUtil.getMethodSign(method);
        TccMethod tccMethod = ObjectCache.get(tccAnnotationMap, methodSign, false, new ObjectCache.Callback<TccMethod>() {
            @Override
            public TccMethod call() throws Exception {
                TccMethod tccMethod = method.getAnnotation(TccMethod.class);
                return tccMethod;
            }
        });
        return tccMethod;
    }
}
