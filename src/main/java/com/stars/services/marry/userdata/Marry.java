package com.stars.services.marry.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.LogUtil;

/**
 * 婚姻数据
 * Created by zhouyaohui on 2016/12/2.
 */
public class Marry extends DbRow implements Cloneable {
    /**
     *改版后婚姻只有两个状态
     *1、预约状态
     *2、已婚状态
     * 如果在婚宴中，以另外字段记录，不再合到一个字段控制
     */

    public final static byte WAIT_WEDDING_ORDER = 0;    // 待预约
    public final static byte WAIT_WEDDING = 1;          // 等待婚宴
    public final static byte WEDDING = 2;               // 婚宴
    public final static byte MARRIED = 3;               // 已婚

    /**
     * 婚宴类型
     */
    public final static byte NOMAL_WEDDING = 1;         //普通婚宴
    public final static byte LUXURIOUS_WEDDING = 2;     //豪华婚宴

    private String uniqueKey;   // 采用 roleid+roleid+timestamp
    private byte state;         // 婚姻状态
    private byte breakState;    // 决裂状态 0友好状态 1 决裂状态 2拒绝决裂
    private int appointStamp;   // 预约的时间戳，用于判断普通预约过期
    /**
     * appointByte  appointRole
     * 1、超时时，给邮件补偿，这个时候清除
     * 2、收到玩家拒绝时，给邮件补偿，这个时候清除
     */
    private byte appointByte;      // 上次预约类型
    private long appointRole;      // 上次预约方
    private int marryStamp;        // 结婚时间戳
    private int shipValue;         // 情谊值
    private long breaker;          // 主动决裂的一方
    private int lastBreakStamp;    // 最后一次决裂时间
    private int breakCount;        // 决裂次数
    private long man;              // 男方
    private long woman;            // 女方
    private byte lastSuccessAppointTyte;  // 上次成功预约类型

    public int getBreakCount() {
        return breakCount;
    }

    public void setBreakCount(int breakCount) {
        this.breakCount = breakCount;
    }

    public long getBreaker() {
        return breaker;
    }

    public void setBreaker(long breaker) {
        this.breaker = breaker;
    }

    public int getLastBreakStamp() {
        return lastBreakStamp;
    }

    public void setLastBreakStamp(int lastBreakStamp) {
        this.lastBreakStamp = lastBreakStamp;
    }

    public int getShipValue() {
        return shipValue;
    }

    public void setShipValue(int shipValue) {
        this.shipValue = shipValue;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        if (this.state == Marry.MARRIED) return;
        this.state = state;
    }

    public byte getBreakState() {
        return breakState;
    }

    public void setBreakState(byte breakState) {
        this.breakState = breakState;
    }

    public int getAppointStamp() {
        return appointStamp;
    }

    public void setAppointStamp(int appointStamp) {
        this.appointStamp = appointStamp;
    }

    public int getMarryStamp() {
        return marryStamp;
    }

    public void setMarryStamp(int marryStamp) {
        this.marryStamp = marryStamp;
    }

    public long getOther(long roleId) {
        String[] key = uniqueKey.split("[+]");
        if (Long.valueOf(key[0]) == roleId) {
            return Long.valueOf(key[1]);
        } else {
            return Long.valueOf(key[0]);
        }
    }

    public void addBreakCount() {
        breakCount++;
    }

    public long getMan() {
        return this.man;
    }

    public void setMan(long man) {
        this.man = man;
    }

    public long getWoman() {
        return this.woman;
    }

    public void setWoman(long woman) {
        this.woman = woman;
    }

    public byte getAppointByte() {
        return this.appointByte;
    }

    public void setAppointByte(byte appointByte) {
        this.appointByte = appointByte;
    }

    public long getAppointRole() {
        return this.appointRole;
    }

    public void setAppointRole(long appointRole) {
        this.appointRole = appointRole;
    }

    public byte getLastSuccessAppointTyte() {return this.lastSuccessAppointTyte;}

    public void setLastSuccessAppointTyte(byte lastSuccessAppointTyte) {this.lastSuccessAppointTyte = lastSuccessAppointTyte;}

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "marry", "uniquekey = '" + uniqueKey + "'");
    }

    @Override
    public String getDeleteSql() {
        return null;
    }

    public Marry copy() {
        try {
            return (Marry) super.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("克隆结婚对象失败。", e);
        }
        return null;
    }
}
