package com.stars.modules.tool.userdata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 玩家某件装备某个符文孔信息
 * Created by zhanghaizhen on 2017/6/9.
 */
public class RoleTokenEquipmentHolePo {
    private byte holeId; //装备上的符文孔位置
    private int tokenId; //符文孔上的符文id
    private int tokenLevel; //符文等级

    public RoleTokenEquipmentHolePo(byte holeId,int tokenId,int tokenLevel){
        this.holeId = holeId;
        this.tokenId = tokenId;
        this.tokenLevel = tokenLevel;
    }

    public RoleTokenEquipmentHolePo(String tokenHoleInfo){
        String[] array = tokenHoleInfo.split(",");
        this.holeId = Byte.parseByte(array[0]);
        this.tokenId = Integer.parseInt(array[1]);
        this.tokenLevel = Integer.parseInt(array[2]);
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

    public int getTokenLevel() {
        return tokenLevel;
    }

    public void setTokenLevel(int tokenLevel) {
        this.tokenLevel = tokenLevel;
    }

    public void addTokenLevel(){
        this.tokenLevel++;
    }

    public String toString(){
        StringBuffer sBuff = new StringBuffer();
        sBuff.append(this.holeId)
                .append(",")
                .append(this.tokenId)
                .append(",")
                .append(this.tokenLevel);
        return sBuff.toString();
    }
    public void writeToBuffer(NewByteBuffer buff){
        buff.writeByte(this.holeId);
        buff.writeInt(this.tokenId);
        buff.writeInt(this.tokenLevel);
    }
}
