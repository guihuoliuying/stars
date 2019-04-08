package com.stars.server.connector.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zws on 2015/9/14.
 */
public class TestArrayAndMap {

    public static void main(String[] args) {
        long[] array = new long[Short.MAX_VALUE + 1];
        long st = System.currentTimeMillis();
        for (int i = 0; i < 40960; i++) {
            for (int j = 0; j <= Short.MAX_VALUE; j++) {
                array[j] = i;
            }
        }
        long et = System.currentTimeMillis();
        System.out.println("array time elapsed: " + (et - st));

        Map<Integer, Integer> map = new HashMap<>(Short.MAX_VALUE);
        st = System.currentTimeMillis();
        for (int i = 0; i < 40960; i++) {
            for (int j = 0; j <= Short.MAX_VALUE; j++) {
                map.put(j, i);
            }
        }
        et = System.currentTimeMillis();
        System.out.println("map time elapsed: " + (et - st));
    }

}
