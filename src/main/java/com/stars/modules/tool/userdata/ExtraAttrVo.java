package com.stars.modules.tool.userdata;

import com.stars.core.attr.FormularUtils;
import com.stars.network.server.buffer.NewByteBuffer;

import java.io.Serializable;

/**
 * Created by wuyuxing on 2016/11/9.
 */
public class ExtraAttrVo implements Serializable,Comparable<ExtraAttrVo> {
    private byte index;
    private byte quality;
    private String attrName;
    private int attrValue;

    private int fighting;

    public ExtraAttrVo() {
    }

    public ExtraAttrVo(byte index, byte quality, String attrName, int attrValue) {
        this.index = index;
        this.quality = quality;
        this.attrName = attrName;
        this.attrValue = attrValue;
        resetFighting();
    }

    public ExtraAttrVo(String strData) {
        String[] array = strData.split(",");
        this.index = Byte.parseByte(array[0]);
        this.quality = Byte.parseByte(array[1]);
        this.attrName = array[2];
        this.attrValue = Integer.parseInt(array[3]);
        resetFighting();
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public byte getQuality() {
        return quality;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public int getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(int attrValue) {
        this.attrValue = attrValue;
    }

    public int getFighting() {
        return fighting;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public void resetFighting(){
        this.fighting = FormularUtils.calcFightScoreByAttr(this.attrName, this.attrValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(index).append(",")
          .append(quality).append(",")
          .append(attrName).append(",")
          .append(attrValue);
        return sb.toString();
    }

    public void writeToBuffer(NewByteBuffer buff){
        buff.writeByte(index);
        buff.writeByte(quality);
        buff.writeString(attrName);
        buff.writeInt(attrValue);
        buff.writeInt(fighting);
    }

    @Override
    public int compareTo(ExtraAttrVo vo) {
        return vo.getFighting() - this.getFighting();
    }
}
