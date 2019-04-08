package com.stars.modules.familyactivities.bonfire.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.bonfire.FamilyBonfirePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by zhouyaohui on 2016/10/9.
 */
public class ClientBonfire extends PlayerPacket {
    /** 常量 */
    public final static byte IDLE = 0;
    public final static byte DROP = 1;  // 掉落物品
    public final static byte BEGIN = 2; // 活动开启
    public final static byte END = 3;   // 活动结束
    public final static byte INIT = 4;  // 篝火活动基本信息初始化
    public final static byte UPDATE = 5;// 刷新篝火活动信息
    public final static byte REFRESH_WOOD = 6;//刷新干柴
    public final static byte QUESTION_INFO = 7;//题目信息
    public final static byte UPDATE_THROW_GOLD_TIMES = 8;//刷新投元宝次数

    private int fireLevel;
    private long fireExp;
    private int exp;
    private long now;
    private long lastUseGoldTimes;
    private long lastUseWoodTimes;
    private long remainTime;
    private int dailyThrowGoldTimes;

    private int questionId;
    private List<Integer> answerList;   //题目序号
    private int remainSecond;
    private int questionIndex;
    private byte activityStatus; //活动状态

    public ClientBonfire() {
    }

    public ClientBonfire(byte resType) {
        this.resType = resType;
    }

    private byte resType;
    private String dropStr;

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setDropStr(String dropStr) {
        this.dropStr = dropStr;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        if (resType == DROP) {
            buff.writeString(dropStr);
        }else if(resType == INIT){
            buff.writeInt(fireLevel);
            buff.writeLong(fireExp);
            buff.writeLong(remainTime);
            buff.writeInt(dailyThrowGoldTimes);
            buff.writeByte(activityStatus);
        }else if(resType == UPDATE){
            buff.writeInt(fireLevel);
            buff.writeLong(fireExp);
            buff.writeInt(exp);
            buff.writeByte(activityStatus);
        }else if(resType == REFRESH_WOOD){

        }else if(resType == QUESTION_INFO){
            buff.writeInt(questionIndex);
            buff.writeInt(questionId);
            buff.writeInt(remainSecond);
            buff.writeInt(answerList.size());
            for(int index:answerList){
                buff.writeInt(index);
            }
        }else if(resType == UPDATE_THROW_GOLD_TIMES){
            buff.writeInt(dailyThrowGoldTimes);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyBonfirePacketSet.C_BONFIRE;
    }

    public void setFireLevel(int fireLevel) {
        this.fireLevel = fireLevel;
    }

    public void setFireExp(long fireExp) {
        this.fireExp = fireExp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public void setLastUseGoldTimes(long lastUseGoldTimes) {
        this.lastUseGoldTimes = lastUseGoldTimes;
    }

    public long getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(long remainTime) {
        this.remainTime = remainTime;
    }

    public void setLastUseWoodTimes(long lastUseWoodTimes) {
        this.lastUseWoodTimes = lastUseWoodTimes;
    }

    public int getRemainSecond() {
        return remainSecond;
    }

    public void setRemainSecond(int remainSecond) {
        this.remainSecond = remainSecond;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public List<Integer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Integer> answerList) {
        this.answerList = answerList;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public int getDailyThrowGoldTimes() {
        return dailyThrowGoldTimes;
    }

    public void setDailyThrowGoldTimes(int dailyThrowGoldTimes) {
        this.dailyThrowGoldTimes = dailyThrowGoldTimes;
    }

    public byte getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(byte activityStatus) {
        this.activityStatus = activityStatus;
    }
}
