package com.stars.modules.newequipment.packet.vo;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.vowriter.BuffUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/16.
 */
public class NextStarInfo {

    public static final byte MAX = 1;

    private byte type;
    private byte isMax;
    private int level;
    private int levelLimit;
    private int attrAdd;
    private int displaySuccess;
    private Map<Integer,Integer> materialMap;
    private Map<Integer,Integer> safeItemMap;

    public NextStarInfo(byte type) {
        this.type = type;
    }

    public NextStarInfo(byte type, byte isMax) {
        this.type = type;
        this.isMax = isMax;
    }

    public static byte getMax() {
        return MAX;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getIsMax() {
        return isMax;
    }

    public void setIsMax(byte isMax) {
        this.isMax = isMax;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAttrAdd() {
        return attrAdd;
    }

    public void setAttrAdd(int attrAdd) {
        this.attrAdd = attrAdd;
    }

    public void setDisplaySuccess(int displaySuccess) {
        this.displaySuccess = displaySuccess;
    }

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public void setMaterialMap(Map<Integer, Integer> materialMap) {
        this.materialMap = materialMap;
    }

    public Map<Integer, Integer> getSafeItemMap() {
        return safeItemMap;
    }

    public void setSafeItemMap(Map<Integer, Integer> safeItemMap) {
        this.safeItemMap = safeItemMap;
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelLimit(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public void writeToBuff(NewByteBuffer buff){
        buff.writeByte(type);
        buff.writeByte(isMax);
        if(isMax == 1) return;
        buff.writeInt(level);
        buff.writeInt(levelLimit);
        buff.writeInt(attrAdd);
        buff.writeInt(displaySuccess);
        BuffUtil.writeIntMapToBuff(buff,materialMap);
        BuffUtil.writeIntMapToBuff(buff,safeItemMap);
    }
}
