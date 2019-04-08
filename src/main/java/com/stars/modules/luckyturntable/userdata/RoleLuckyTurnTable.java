package com.stars.modules.luckyturntable.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.luckyturntable.LuckyTurnTableManager;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class RoleLuckyTurnTable extends DbRow {
    private long roleId;
    private String luckyId;
    private int lottery;
    private int accMoney;

    private Map<Integer, Byte> luckyIdMap = new HashMap<>();

    public RoleLuckyTurnTable() {
    }

    public RoleLuckyTurnTable(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getLuckyId() {
        return luckyId;
    }

    public void setLuckyId(String luckyId) {
        this.luckyId = luckyId;
        this.luckyIdMap = StringUtil.toMap(luckyId, Integer.class, Byte.class, '+', '|');
    }

    public void updateLockyId(int id) {
        this.luckyIdMap.put(id, LuckyTurnTableManager.DRAWN);
        this.luckyId = StringUtil.makeString(luckyIdMap, '+', '|');
    }

    public void putAllLuckyId(List<Integer> luckyIds) {
        for (int id : luckyIds) {
            if (luckyIdMap.containsKey(id)) continue;
            this.luckyIdMap.put(id, LuckyTurnTableManager.NOTDRAWN);
        }
        this.luckyId = StringUtil.makeString(luckyIdMap, '+', '|');
    }

    public int getTurnCount() {
        int count = 0;
        for (byte b : luckyIdMap.values()) {
            if (b == LuckyTurnTableManager.DRAWN) {
                count++;
            }
        }
        return count;
    }

    public Set<Integer> getUnTurnIdMap() {
        Set<Integer> tmp = new HashSet<>();
        for (Map.Entry<Integer, Byte> entry : luckyIdMap.entrySet()) {
            if (entry.getValue() == LuckyTurnTableManager.NOTDRAWN) {
                tmp.add(entry.getKey());
            }
        }
        return tmp;
    }

    public byte getDRAWAN_OR_NOT(int id) {
        return luckyIdMap.get(id);
    }

    public Map<Integer, Byte> getLuckyIdMap() {
        return luckyIdMap;
    }

    public int getLottery() {
        return lottery;
    }

    public void setLottery(int lottery) {
        this.lottery = lottery;
    }

    public void addLottery(int lottery) {
        this.lottery += lottery;
    }

    public void decLottery(int decLottery) {
        this.lottery -= decLottery;
    }

    public int getAccMoney() {
        return accMoney;
    }

    public void setAccMoney(int accMoney) {
        this.accMoney = accMoney;
    }

    public void addAccMoney(int money) {
        this.accMoney += money;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckyturntable", " `roleid` = " + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from roleluckyturntable where `roleid` = " + this.roleId;
    }

    @Override
    public String toString() {
        return "RoleLuckyTurnTable{" +
                "roleId=" + roleId +
                ", luckyId='" + luckyId + '\'' +
                ", lottery=" + lottery +
                ", accMoney=" + accMoney +
                "} ";
    }
}
