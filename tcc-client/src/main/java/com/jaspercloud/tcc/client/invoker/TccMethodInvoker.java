package com.jaspercloud.tcc.client.invoker;

import com.jaspercloud.tcc.core.util.TccConstants;

import java.lang.reflect.Method;

public class TccMethodInvoker {

    private String uniqueName;
    private Object target;
    private Method tryMethod;
    private Method confirmMethod;
    private Method cancelMethod;

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

    public TccMethodInvoker(String uniqueName, Object target) {
        this.uniqueName = uniqueName;
        this.target = target;
    }

    public void invoke(TccConstants.TccStatus tccStatus, Object[] args) throws Exception {
        if (TccConstants.TccStatus.Try.equals(tccStatus)) {
            tryMethod.invoke(target, args);
        } else if (TccConstants.TccStatus.Confirm.equals(tccStatus)) {
            confirmMethod.invoke(target, args);
        } else if (TccConstants.TccStatus.Cancel.equals(tccStatus)) {
            cancelMethod.invoke(target, args);
        } else {
            throw new UnsupportedOperationException(tccStatus.toString());
        }
    }
}
