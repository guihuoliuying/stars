package com.stars.modules.daily.userdata;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/7.
 */
public class RoleTmpDayInfo implements Comparable<RoleTmpDayInfo>{
    private short dailyid;
    private int firstScore; //第一次通过可获得斗魂值
    private int doneCount; //完成次数
    private byte isSuperAward;  //是否超级奖励
    private byte isMutipleAward; //是否多倍奖励
    private int mutipleCount; //奖励倍数
    private int fightDelta; //战力差（比推荐战力少多少）
    private int rank; //战力差比较的下级 排序比较
    private Map<Integer,Integer> awardMap;

    public void writeToBuff(NewByteBuffer buff){
        buff.writeShort(dailyid);
        buff.writeInt(doneCount);
        buff.writeByte(isSuperAward);
        buff.writeByte(isMutipleAward);
        buff.writeInt(mutipleCount);
        buff.writeInt(firstScore);
        int awardSize = awardMap.size();
        buff.writeInt(awardSize);
        for(Map.Entry<Integer,Integer> entry:awardMap.entrySet()){
            buff.writeInt(entry.getKey());
            buff.writeInt(entry.getValue());
        }

    }

    public short getDailyid() {
        return dailyid;
    }

    public void setDailyid(short dailyid) {
        this.dailyid = dailyid;
    }

    public int getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(int doneCount) {
        this.doneCount = doneCount;
    }

    public byte getIsSuperAward() {
        return isSuperAward;
    }

    public void setIsSuperAward(byte isSuperAward) {
        this.isSuperAward = isSuperAward;
    }

    public byte getIsMutipleAward() {
        return isMutipleAward;
    }

    public void setIsMutipleAward(byte isMutipleAward) {
        this.isMutipleAward = isMutipleAward;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public int getMutipleCount() {
        return mutipleCount;
    }

    public void setMutipleCount(int mutipleCount) {
        this.mutipleCount = mutipleCount;
    }

    public int getFightDelta() {
        return fightDelta;
    }

    public void setFightDelta(int fightDelta) {
        this.fightDelta = fightDelta;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getFirstScore() {
        return firstScore;
    }

    public void setFirstScore(int firstScore) {
        this.firstScore = firstScore;
    }

    @Override
    public int compareTo(RoleTmpDayInfo o) {

        if(fightDelta == o.getFightDelta())
            return  o.getRank() - this.rank;
        return  o.getFightDelta() - this.fightDelta;
    }
}
