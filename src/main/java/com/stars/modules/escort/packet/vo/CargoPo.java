package com.stars.modules.escort.packet.vo;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.vowriter.BuffUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/8.
 */
public class CargoPo {
    private byte index;
    private int carId;         //镖车id
    private int fighting;       //队伍战力
    private byte isRobot;       //是否为机器人

    //玩家镖车变量
    private byte statue;          //镖车状态  0正常  1战斗中  2保护中
    private byte isEnemy;       //是否为仇人 0不是 1是
    private String fightId;     //玩家镖车唯一标识
    private long escortId;      //镖车玩家id
    private String escortName;  //镖车玩家名字

    //机器人镖车变量
    private int sectionId;        //机器人镖车ai唯一id
    private Map<Integer,Integer> award; //机器人镖车奖励

    public void writeToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(index);
        buff.writeInt(carId);
        buff.writeInt(fighting);
        buff.writeByte(isRobot);
        if(isRobot == 0){
            buff.writeByte(statue);
            buff.writeByte(isEnemy);
            buff.writeString(fightId);
            buff.writeString(Long.toString(escortId));
            buff.writeString(escortName);
        }else{
            buff.writeInt(sectionId);
            BuffUtil.writeIntMapToBuff(buff,award);
        }
    }

    public void readFromBuff(NewByteBuffer buff) {
        index = buff.readByte();
        carId = buff.readInt();
        fighting = buff.readInt();
        isRobot = buff.readByte();
        if(isRobot == 0){
            statue = buff.readByte();
            isEnemy = buff.readByte();
            fightId = buff.readString();
            escortId = Long.parseLong(buff.readString());
            escortName = buff.readString();
        }else{
            sectionId = buff.readInt();
            int size = buff.readInt();
            award = new HashMap<>(size);
            int itemId,count;
            if(size > 0) {
                for (int i = 1; i <= size; i++) {
                    itemId = buff.readInt();
                    count = buff.readInt();
                    award.put(itemId, count);
                }
            }
        }
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getFighting() {
        return fighting;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public byte getStatue() {
        return statue;
    }

    public void setStatue(byte statue) {
        this.statue = statue;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public long getEscortId() {
        return escortId;
    }

    public void setEscortId(long escortId) {
        this.escortId = escortId;
    }

    public byte getIsRobot() {
        return isRobot;
    }

    public void setIsRobot(byte isRobot) {
        this.isRobot = isRobot;
    }

    public byte getIsEnemy() {
        return isEnemy;
    }

    public void setIsEnemy(byte isEnemy) {
        this.isEnemy = isEnemy;
    }

    public Map<Integer, Integer> getAward() {
        return award;
    }

    public void setAward(Map<Integer, Integer> award) {
        this.award = award;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getEscortName() {
        return escortName;
    }

    public void setEscortName(String escortName) {
        this.escortName = escortName;
    }
}
