package com.tigergraph.spark_connector.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class StateStorage {

    private long startTime = System.currentTimeMillis();
    private AtomicInteger successNum = new AtomicInteger(0);
    private AtomicInteger failNum = new AtomicInteger(0);
    private AtomicInteger totalNum = new AtomicInteger(0);
    private volatile Boolean exit = false;
    private volatile Boolean statusChange = false;

    public void successOne() {
        successNum.incrementAndGet();
        statusChange = true;
    }

    public void failOne() {
        failNum.incrementAndGet();
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public void setStatusChange(Boolean statusChange) {
        this.statusChange = statusChange;
    }

    public void setTotalNum(int num) {
        this.totalNum.set(num);
    }

    public long getStartTime() {
        return startTime;
    }

    public AtomicInteger getSuccessNum() {
        return successNum;
    }

    public AtomicInteger getFailNum() {
        return failNum;
    }

    public AtomicInteger getTotalNum() {
        return totalNum;
    }

    public Boolean getExit() {
        return exit;
    }

    public Boolean getStatusChange() {
        return statusChange;
    }
}
