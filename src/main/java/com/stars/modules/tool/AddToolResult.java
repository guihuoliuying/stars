package com.stars.modules.tool;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/19.
 */
public class AddToolResult {
    private Map<Integer,Integer> realGetMap;    //实际获得的物品列表
    private int addCount;   //实际增加的物品数量
    private int resCount;   //剩余未增加的物品数量

    public AddToolResult(Map<Integer, Integer> realGetMap) {
        this.realGetMap = realGetMap;
    }

    public Map<Integer, Integer> getRealGetMap() {
        return realGetMap;
    }

    public void setRealGetMap(Map<Integer, Integer> realGetMap) {
        this.realGetMap = realGetMap;
    }

    public int getAddCount() {
        return addCount;
    }

    public void setAddCount(int addCount) {
        this.addCount = addCount;
    }

    public int getResCount() {
        return resCount;
    }

    public void setResCount(int resCount) {
        this.resCount = resCount;
    }
}
