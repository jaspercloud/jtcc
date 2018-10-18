package com.japsercloud.tcc.server.support;

public interface Lock {

    boolean isLock();

    void lock();

    long tryLock();

    void unlock();
}
