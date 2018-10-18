package com.github.jaspercloud.tcc.dubbo.demo.exception;

public class InvokeException extends RuntimeException {

    public InvokeException() {
    }

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeException(Throwable cause) {
        super(cause);
    }
}
