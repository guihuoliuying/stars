package com.stars.modules.scene.prodata;

import java.util.HashMap;
import java.util.Map;

/**
 * 复活配置
 * Created by liuyuheng on 2016/10/8.
 */
public class ReviveConfig {
    private byte stageType;// 战斗场景类型
    private byte freeNum;// 免费次数
    private byte limitNum;// 限制次数
    private Map<Byte, Map<Integer, Integer>> costMap = new HashMap<>();

    public ReviveConfig(byte stageType, byte freeNum, byte limitNum, Map<Byte, Map<Integer, Integer>> costMap) {
        this.stageType = stageType;
        this.freeNum = freeNum;
        this.limitNum = limitNum;
        this.costMap = costMap;
    }

    public byte getStageType() {
        return stageType;
    }

    public byte getFreeNum() {
        return freeNum;
    }

    public byte getLimitNum() {
        return limitNum;
    }

    public Map<Integer, Integer> getCost(byte index) {
    	byte maxIndex = (byte)costMap.size();
    	if (index > maxIndex) {
			index = maxIndex;
		}
        return costMap.get(index);
    }
}
