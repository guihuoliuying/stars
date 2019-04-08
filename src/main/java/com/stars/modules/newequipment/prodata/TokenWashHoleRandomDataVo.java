package com.stars.modules.newequipment.prodata;

import com.stars.util.RandomUtil;

/**
 * 符文孔随机信息，信息来自TokenRandomRangeVo：tokenLevelRange
 * Created by zhanghaizhen on 2017/6/10.
 */
public class TokenWashHoleRandomDataVo {
    public byte holeId;
    public int tokenId;
    public int minLevel;
    public int maxLevel;

    public TokenWashHoleRandomDataVo(String initString){
        String[] array = initString.split("\\+");
        this.holeId = Byte.parseByte(array[0]);
        this.tokenId = Integer.parseInt(array[1]);
        this.minLevel = Integer.parseInt(array[2]);
        this.maxLevel = Integer.parseInt(array[3]);
    }

    public int getRandomLevel(){
        return RandomUtil.rand(minLevel,maxLevel);
    }

    public byte getHoleId() {
        return holeId;
    }

    public void setHoleId(byte holeId) {
        this.holeId = holeId;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
}
