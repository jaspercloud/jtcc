package com.jaspercloud.tcc.core.util;

import com.jaspercloud.tcc.core.support.transaction.TccPlatformTransactionManager;

import java.util.Stack;

public final class TccContext {

    private static ThreadLocal<Stack<TccContext>> threadLocal = new InheritableThreadLocal<Stack<TccContext>>() {

        @Override
        protected Stack<TccContext> initialValue() {
            return new Stack<>();
        }
    };

    private boolean originator;
    private String tid;
    private String uniqueName;
    private TccConstants.TccStatus tccStatus;
    private TccPlatformTransactionManager transactionManager;

    public boolean isOriginator() {
        return originator;
    }

    public String getTid() {
        return tid;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public TccConstants.TccStatus getTccStatus() {
        return tccStatus;
    }

    public void setTccStatus(TccConstants.TccStatus tccStatus) {
        this.tccStatus = tccStatus;
    }

    public TccPlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TccPlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private TccContext(String tid, boolean originator) {
        this.tid = tid;
        this.originator = originator;
    }

    public static TccContext create(String tid, boolean originator) {
        TccContext tccContext = new TccContext(tid, originator);
        threadLocal.get().push(tccContext);
        return tccContext;
    }

    public static TccContext joinTccContext(TccContext tccContext) {
        TccContext join = new TccContext(tccContext.getTid(), false);
        threadLocal.get().push(join);
        return join;
    }

    public static TccContext get() {
        if (threadLocal.get().empty()) {
            return null;
        }
        TccContext tccContext = threadLocal.get().peek();
        return tccContext;
    }

    public static void release() {
        threadLocal.get().pop();
    }

    public static void clean() {
        threadLocal.remove();
    }
}
