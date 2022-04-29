package com.tigergraph.spark_connector.utils;

import java.util.concurrent.TimeUnit;

public class SleepTools {
    public static void ms(long ms){
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void seconds(long s){
        try {
            TimeUnit.SECONDS.sleep(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
