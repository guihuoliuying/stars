package com.stars.modules.opactsecondskill.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class RoleSecondSkillPo extends DbRow {
    private long roleId;
    private int totalPay; //累计的充值数
    private int lastBuyCharge; //上次购买所属额度
    private String record; //推送数据
    private long resetTimeStamp;//重置时间戳
    //内存
    private Map<Integer, Integer> recordMap = new HashMap<>();
    private int maxPushCharge; //记录玩家当前推送最高档位

    public RoleSecondSkillPo() {
    }

    public RoleSecondSkillPo(long roleId) {
        this.roleId = roleId;
        recordMap = new HashMap<>();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolesecondkill", " `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolesecondkill", " `roleid`=" + roleId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(int totalPay) {
        this.totalPay = totalPay;
    }

    public int getLastBuyCharge() {
        return lastBuyCharge;
    }

    public void setLastBuyCharge(int lastBuyCharge) {
        this.lastBuyCharge = lastBuyCharge;
    }

    public long getResetTimeStamp() {
        return resetTimeStamp;
    }

    public void setResetTimeStamp(long resetTimeStamp) {
        this.resetTimeStamp = resetTimeStamp;
    }

    public String getRecord() {
        if (StringUtil.isEmpty(recordMap))
            return "";
        record = StringUtil.makeString(recordMap, '=', '|');
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
        recordMap = StringUtil.toMap(record, Integer.class, Integer.class, '=', '|');
    }

    public int getMaxPushCharge() {
        return maxPushCharge;
    }

    public void setMaxPushCharge(int maxPushCharge) {
        this.maxPushCharge = maxPushCharge;
    }

    public Map<Integer, Integer> getRecordMap() {
        if (recordMap == null) {
            recordMap = new HashMap<>();
        }
        return recordMap;
    }

    public void setRecordMap(Map<Integer, Integer> recordMap) {
        this.recordMap = recordMap;
    }

    public void reset() {
        this.record = "";
        recordMap.clear();
        lastBuyCharge = 0;
        totalPay = 0;
        resetTimeStamp = 0L;
    }
}
