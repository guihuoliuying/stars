package com.stars.modules.drop.prodata;

/**
 * 掉落奖励
 * Created by liuyuheng on 2016/6/30.
 */
public class DropRewardVo {
    private byte type;// 类型 0=物品,1=掉落组
    private int rewardId;// 物品Id
    private int power;// 概率/权值
    private int number;// 数量

    public DropRewardVo(String reward) {
        String[] tempStr = reward.split("\\+");
        this.type = Byte.parseByte(tempStr[0]);
        this.rewardId = Integer.parseInt(tempStr[1]);
        this.power = Integer.parseInt(tempStr[2]);
        this.number = Integer.parseInt(tempStr[3]);
    }

    public byte getType() {
        return type;
    }

    public int getRewardId() {
        return rewardId;
    }

    public int getNumber() {
        return number;
    }

    public int getPower() {
        return power;
    }
}

