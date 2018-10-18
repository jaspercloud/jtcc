package com.japsercloud.tcc.server.config;

import com.jaspercloud.tcc.core.config.TccConfigProperties;

public class TccServerConfigProperties extends TccConfigProperties {

    private String nameSpace = "tcc";
    private String zookeeperAddress = "127.0.0.1:2181";
    private String zookeeperPath = "/tcc";
    private int coordinatorThreads = 300;
    private int scanLimit = 1000;
    private long compensateTimeout = 30 * 1000;
    private long lockTime = 30 * 1000;
    private int followers = 1;
    private int followerQueue = 1000;

    public TccServerConfigProperties() {
        super();
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public String getZookeeperPath() {
        return zookeeperPath;
    }

    public void setZookeeperPath(String zookeeperPath) {
        this.zookeeperPath = zookeeperPath;
    }

    public int getCoordinatorThreads() {
        return coordinatorThreads;
    }

    public void setCoordinatorThreads(int coordinatorThreads) {
        this.coordinatorThreads = coordinatorThreads;
    }

    public int getScanLimit() {
        return scanLimit;
    }

    public void setScanLimit(int scanLimit) {
        this.scanLimit = scanLimit;
    }

    public long getCompensateTimeout() {
        return compensateTimeout;
    }

    public void setCompensateTimeout(long compensateTimeout) {
        this.compensateTimeout = compensateTimeout;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowerQueue() {
        return followerQueue;
    }

    public void setFollowerQueue(int followerQueue) {
        this.followerQueue = followerQueue;
    }
}
