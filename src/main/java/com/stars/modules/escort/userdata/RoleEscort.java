package com.stars.modules.escort.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.escort.EscortManager;
import com.stars.modules.escort.userdata.vo.CargoRecord;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/3.
 */
public class RoleEscort extends DbRow {
    private long roleId;
    private int dailyEscortTimes;   //今日参与运镖次数
    private int dailyRobTimes;      //今日参与劫镖次数
    private int dailyFreshTimes;    //今日镖车刷新次数
    private Map<Byte, CargoRecord> cargoRecordMap;   //镖车选择记录
    private long cargoRecordResetTime;  //镖车重置时间

    public RoleEscort() {
    }

    public RoleEscort(long roleId) {
        this.roleId = roleId;
        this.cargoRecordMap = new HashMap<>(4);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDailyEscortTimes() {
        return dailyEscortTimes;
    }

    public void setDailyEscortTimes(int dailyEscortTimes) {
        this.dailyEscortTimes = dailyEscortTimes;
    }

    public int getDailyRobTimes() {
        return dailyRobTimes;
    }

    public void setDailyRobTimes(int dailyRobTimes) {
        this.dailyRobTimes = dailyRobTimes;
    }

    public String getCargoRecord(){
        if(StringUtil.isEmpty(cargoRecordMap)) return "";
        StringBuilder sb = new StringBuilder();
        CargoRecord record;
        for(byte index = 1;index<=4;index++){
            record = cargoRecordMap.get(index);
            if(record == null) continue;
            sb.append(index).append("\\+")
              .append(record.getCargoId()).append("\\+")
              .append(record.getHasUsed()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public void setCargoRecord(String cargoRecord){
        this.cargoRecordMap = new HashMap<>(4);
        if(StringUtil.isEmpty(cargoRecord)) return;

        String[] recordData = cargoRecord.split(",");
        String[] cargoData;
        CargoRecord record;
        for(String recordStr:recordData){
            cargoData = recordStr.split("\\+");
            if(cargoData == null || cargoData.length != 3) continue;
            record = new CargoRecord(Integer.parseInt(cargoData[1]),Byte.parseByte(cargoData[2]));
            cargoRecordMap.put(Byte.parseByte(cargoData[0]),record);
        }
    }

    public long getCargoRecordResetTime() {
        return cargoRecordResetTime;
    }

    public void setCargoRecordResetTime(long cargoRecordResetTime) {
        this.cargoRecordResetTime = cargoRecordResetTime;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleescort", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleescort", "`roleid`=" + roleId);
    }

    public void dailyReset(){
        this.dailyEscortTimes = 0;
        this.dailyRobTimes = 0;
        this.dailyFreshTimes = 0;
    }

    public void addEscortTimes(){
        this.dailyEscortTimes++;
    }

    public void addRobTimes(){
        this.dailyRobTimes++;
    }

    public Map<Byte, CargoRecord> getCargoRecordMap() {
        return cargoRecordMap;
    }

    public void setCargoRecordMap(Map<Byte, CargoRecord> cargoRecordMap) {
        this.cargoRecordMap = cargoRecordMap;
    }

    /**
     * 获得今日剩余运镖次数
     */
    public int getRemainTime(){
        int remainTime = EscortManager.getCargocarDayCount() - dailyEscortTimes;
        if(remainTime < 0) remainTime = 0;
        return remainTime;
    }

    /**
     * 获得今日剩余劫镖次数
     */
    public int getRemainRobTime(){
        int robTime = EscortManager.getCargocarRobCount() - dailyRobTimes;
        if(robTime < 0) robTime = 0;
        return robTime;
    }

    public int getDailyFreshTimes() {
        return dailyFreshTimes;
    }

    public void setDailyFreshTimes(int dailyFreshTimes) {
        this.dailyFreshTimes = dailyFreshTimes;
    }

    public void addDailyFreshTimes(){
        this.dailyFreshTimes++;
    }
}
