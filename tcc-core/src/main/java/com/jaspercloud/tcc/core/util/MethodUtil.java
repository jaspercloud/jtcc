package com.jaspercloud.tcc.core.util;

import java.lang.reflect.Method;

public final class MethodUtil {

    private MethodUtil() {

    }

    public static String[] encodeArgsTypes(Class<?>[] classes) {
        String[] argsTypes = ClassTypeUtils.getTypeStrs(classes);
        return argsTypes;
    }

    public static Class<?>[] decodeArgsTypes(String[] argsTypes) throws Exception {
        Class[] classes = ClassTypeUtils.getClasses(argsTypes);
        return classes;
    }

    public static String getMethodSign(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        String className = clazz.getName();
        String methodName = method.getName();
        String[] argsTypes = encodeArgsTypes(method.getParameterTypes());
        StringBuilder builder = new StringBuilder();
        builder.append(className).append(".").append(methodName);
        builder.append("(");
        if (argsTypes.length > 0) {
            for (String argType : argsTypes) {
                builder.append(argType).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        String sign = builder.toString();
        return sign;
    }
}
