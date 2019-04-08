package com.stars.modules.newequipment.packet.vo;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/16.
 */
public class NextStrengthInfo {

    public static final byte MAX = 1;

    private byte type;
    private byte isMax;
    private int level;
    private int attrPercent;
    private int attrAdd;
    private int levelLimit;
    private Map<Integer,Integer> materialMap;

    public NextStrengthInfo(byte type,byte isMax) {
        this.type = type;
        this.isMax = isMax;
    }

    public NextStrengthInfo(byte type) {
        this.type = type;
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

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public void setMaterialMap(Map<Integer, Integer> materialMap) {
        this.materialMap = materialMap;
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelLimit(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public int getAttrPercent() {
        return attrPercent;
    }

    public void setAttrPercent(int attrPercent) {
        this.attrPercent = attrPercent;
    }

    public void writeToBuff(NewByteBuffer buff){
        buff.writeByte(type);
        buff.writeByte(isMax);
        if(isMax == 1) return;
        buff.writeInt(level);
        buff.writeInt(levelLimit);
        buff.writeInt(attrPercent);
        buff.writeInt(attrAdd);
        if(StringUtil.isEmpty(materialMap)){
            buff.writeByte((byte)0);
        }else{
            buff.writeByte((byte)materialMap.size());
            for(Map.Entry<Integer,Integer> entry:materialMap.entrySet()){
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }
    }
}
