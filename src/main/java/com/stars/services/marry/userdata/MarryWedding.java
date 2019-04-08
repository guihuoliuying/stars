package com.stars.services.marry.userdata;

import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/12/7.
 */
public class MarryWedding implements Cloneable {

    public final static byte WAIT = 0;
    public final static byte PREPARE = 1;
    public final static byte RUN = 2;
    public final static byte END = 3;
    public final static byte MISS = 4;

    private long order; // 预约的角色
    private long other; // 被预约的角色
    private byte state; // 婚礼的状态

    private String marryKey;  // 结婚key

    private long startStamp; // 开始时间戳
    private Set<Long> party = new HashSet<>();  // 参加婚礼的玩家

    // 婚礼活动数据不入库
    private Map<Long, Integer> redbagMap = new HashMap<>(); // 角色发红包次数
    private Map<Long, Long> redbagGetMap = new HashMap<>();  // 角色抢红包map  roleId <-> lasttimestamp
    private Map<Long, Integer> fireworksMap = new HashMap<>();  // 烟花次数
    private Map<Long,Integer> redbagGetTimesMap = new HashMap<>(); // 角色在一轮红包中获得奖励次数

    public Map<Long, Integer> getFireworksMap() {
        return fireworksMap;
    }

    public Map<Long, Long> getRedbagGetMap() {
        return redbagGetMap;
    }

    public Map<Long, Integer> getRedbagMap() {
        return redbagMap;
    }

    public Map<Long, Integer> getRedbagTimesMap() {
        return redbagGetTimesMap;
    }

    private Set<String> candySet = new HashSet<>(); // 喜糖存放的set

    private long redbagSender;  // 发红包的人

    private int redbagRemain;   // 剩余红包次数

    private int lastCandyActivity;  // 最后一次喜糖活动时间

    private int lastRedbagStamp;    // 最后一次发红包的时间戳

    private int lastFireworksStamp; // 最后一次烟花

    public Set<String> getCandySet() {return candySet;}

    public void setCandySet(Set<String> candySet) {this.candySet = candySet;}

    public void clearCandySet() {this.candySet.clear();}

    public long getRedbagSender() {return this.redbagSender;}

    public void setRedbagSender(long redbagSender) {this.redbagSender = redbagSender;}

    public int getRedbagRemain() {return this.redbagRemain;}

    public void setRedbagRemain(int redbagRemain) {this.redbagRemain = redbagRemain;}

    public int getLastCandyActivity() {return this.lastCandyActivity;}

    public void setLastCandyActivity(int lastCandyActivity) {this.lastCandyActivity = lastCandyActivity;}

    public int getLastRedbagStamp() {return this.lastRedbagStamp;}

    public void setLastRedbagStamp(int lastRedbagStamp) {this.lastRedbagStamp = lastRedbagStamp;}

    public int getLastFireworksStamp() {return this.lastFireworksStamp;}

    public void setLastFireworksStamp(int lastFireworksStamp) {this.lastFireworksStamp = lastFireworksStamp;}

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public long getOther() {
        return other;
    }

    public void setOther(long other) {
        this.other = other;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getMarryKey() {
        return marryKey;
    }

    public void setMarryKey(String marryKey) {
        this.marryKey = marryKey;
    }

    public boolean isMyWedding(long roleId) {
        String[] key = marryKey.split("[+]");
        if (roleId == Long.valueOf(key[0]) || roleId == Long.valueOf(key[1])) {
            return true;
        }
        return false;
    }

    public MarryWedding copy() {
        try {
            return (MarryWedding) super.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("克隆婚礼对象失败。", e);
        }
        return null;
    }

    public long getStartStamp() {
        return this.startStamp;
    }

    public void setStartStamp(long startStamp) {
        this.startStamp = startStamp;
    }

    public Set<Long> getParty() {
        return this.party;
    }

    public Set<Long> getWeddingUnit() {
        Set<Long> unit = new HashSet<>();
        String[] key = marryKey.split("[+]");
        unit.add(Long.valueOf(key[0]));
        unit.add(Long.valueOf(key[1]));
        return unit;
    }

    public void enter(long roleId) {
        party.add(roleId);
    }

    public void exit(long roleId) {
        party.remove(roleId);
    }
}
