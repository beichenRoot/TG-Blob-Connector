package com.tigergraph.spark_connector.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogDaemon extends Thread {
    private Logger Log = Logger.getLogger("LogDaemon");
    private StateStorage stateStorage;

    public LogDaemon(StateStorage stateStorage) {
        this.stateStorage = stateStorage;
        Log.setLevel(Level.INFO);
    }

    @Override
    public void run() {
        boolean flag = true;

        //  隔多久打印一次 即使没有成功的
        int remainTime = 5000;
        long lastLogTime = System.currentTimeMillis();

        while (flag) {
            // 1 second check once
            SleepTools.ms(1000);
            // If there is a successful write or more than 5S after the last print
            if (stateStorage.getStatusChange() || (System.currentTimeMillis() - lastLogTime) > remainTime) {
                int success = stateStorage.getSuccessNum().get();
                int fail = stateStorage.getFailNum().get();
                int total = stateStorage.getTotalNum().get();

                Log.info(ProgressUtil.drawLine((success * 100 / total)));

                if (((success + fail) == total) || stateStorage.getExit()) {
                    flag = false;
                }
                // put status to false, represent one write success flag
                stateStorage.setStatusChange(false);
                lastLogTime = System.currentTimeMillis();
            }
        }
    }

}
