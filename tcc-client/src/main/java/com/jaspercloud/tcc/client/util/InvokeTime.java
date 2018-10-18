package com.jaspercloud.tcc.client.util;

import java.util.Stack;

public final class InvokeTime {

    private static ThreadLocal<InvokeTime> invokeTimeThreadLocal = new InheritableThreadLocal<InvokeTime>() {
        @Override
        protected InvokeTime initialValue() {
            return new InvokeTime();
        }
    };

    public static InvokeTime get() {
        return invokeTimeThreadLocal.get();
    }

    public static void remove() {
        invokeTimeThreadLocal.remove();
    }

    private Stack<Long> stack = new Stack<>();

    public InvokeTime() {

    }

    public void save() {
        stack.push(System.currentTimeMillis());
    }

    public void peekTime(String tag) {
        Long start = stack.peek();
        long diffTime = System.currentTimeMillis() - start;
        System.out.println(tag + " time: " + diffTime);
    }

    public void popTime(String tag) {
        Long start = stack.pop();
        long diffTime = System.currentTimeMillis() - start;
        System.out.println(tag + " time: " + diffTime);
    }
}
