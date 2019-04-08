package com.stars.modules.oldplayerback.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/7/13.
 */
public class OldPlayerRewardPo extends DbRow {
    private String account;
    private long roleId;
    private String takeRewardRecord;//领奖记录，+分隔
    private int state;
    private long lastTakeTime;//上次的领取
    Map<Integer, Integer> takeRewardMap;

    public OldPlayerRewardPo(String account, long roleId, int state) {
        this.account = account;
        this.roleId = roleId;
        this.state = state;
        Map<Integer, Integer> takeRewardMapTmp = new LinkedHashMap<>();
        for (int index = 1; index <= 7; index++) {
            takeRewardMapTmp.put(index, 1);
        }
        setTakeRewardMap(takeRewardMapTmp);
    }

    public OldPlayerRewardPo() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public Map<Integer, Integer> getTakeRewardMap() {
        return takeRewardMap;
    }

    public void setTakeRewardMap(Map<Integer, Integer> takeRewardMap) {
        this.takeRewardMap = takeRewardMap;
        this.takeRewardRecord = StringUtil.makeString(takeRewardMap, '+', '|');
    }

    /**
     * 返还领奖天数
     * @return
     */
    public int takeReward() {
        for (Map.Entry<Integer, Integer> entry : this.takeRewardMap.entrySet()) {
            if (entry.getValue() == 1) {
                this.takeRewardMap.put(entry.getKey(), 0);
                setTakeRewardMap(this.takeRewardMap);
                setLastTakeTime(System.currentTimeMillis());
                return entry.getKey();
            }
        }

        return 0;
    }

    /**
     * 下次可领取
     * @return
     */
    public int getNextTakeRewardPosition() {
        for (Map.Entry<Integer, Integer> entry : this.takeRewardMap.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }

        return 0;
    }

    public String getTakeRewardRecord() {
        return takeRewardRecord;
    }

    public void setTakeRewardRecord(String takeRewardRecord) {
        this.takeRewardRecord = takeRewardRecord;
        try {
            takeRewardMap = StringUtil.toLinkedHashMap(takeRewardRecord, Integer.class, Integer.class, '+', '|');
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public long getLastTakeTime() {
        return lastTakeTime;
    }

    public void setLastTakeTime(long lastTakeTime) {
        this.lastTakeTime = lastTakeTime;
    }

    public boolean checkTime() {
        return DateUtil.getRelativeDifferDays(new Date(lastTakeTime), new Date()) >= 1;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "oldplayerreward", " account='" + account + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("oldplayerreward", " account='" + account + "'");
    }
}
