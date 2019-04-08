package com.stars.services.family.welfare.redpacket.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public class FamilyRedPacketRecordPo extends DbRow implements Comparable<FamilyRedPacketRecordPo> {

    /* Db */
    private long familyId; // 家族id
    private long redPacketId; // 红包id
    private long giverId; // 派发者姓名
    private String giverName; // 派发者姓名
    private int timestamp; // 时间戳
    private int count; // 可抢个数
    private int seizedCount; // 已抢个数

    /* Mem */
    private List<FamilyRedPacketSeizedRecordPo> seizedRecordPoList = new ArrayList<>(); // 抢红包记录
    private Map<Long, FamilyRedPacketSeizedRecordPo> seizedRecordPoMap = new HashMap<>(); // 抢红包记录

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyredpacketrecord", "`familyid`=" + familyId + " and `redpacketid`=" + redPacketId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyredpacketrecord` where `familyid`=" + familyId + " and `redpacketid`=" + redPacketId;
    }

    public FamilyRedPacketRecordPo copy() {
        FamilyRedPacketRecordPo recordPo = new FamilyRedPacketRecordPo();
        recordPo.setFamilyId(familyId);
        recordPo.setRedPacketId(redPacketId);
        recordPo.setGiverId(giverId);
        recordPo.setGiverName(giverName);
        recordPo.setTimestamp(timestamp);
        recordPo.setCount(count);
        recordPo.setSeizedCount(seizedCount);
        return recordPo;
    }

    @Override
    public int compareTo(FamilyRedPacketRecordPo other) {
        return this.timestamp - other.timestamp;
    }

    /* Mem Data Setter/Getter */
    public List<FamilyRedPacketSeizedRecordPo> getSeizedRecordPoList() {
        return seizedRecordPoList;
    }

    public void setSeizedRecordPoList(List<FamilyRedPacketSeizedRecordPo> seizedRecordPoList) {
        this.seizedRecordPoList = seizedRecordPoList;
    }

    public Map<Long, FamilyRedPacketSeizedRecordPo> getSeizedRecordPoMap() {
        return seizedRecordPoMap;
    }

    public void setSeizedRecordPoMap(Map<Long, FamilyRedPacketSeizedRecordPo> seizedRecordPoMap) {
        this.seizedRecordPoMap = seizedRecordPoMap;
    }

    /* Db Data Setter/Getter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(long redPacketId) {
        this.redPacketId = redPacketId;
    }

    public long getGiverId() {
        return giverId;
    }

    public void setGiverId(long giverId) {
        this.giverId = giverId;
    }

    public String getGiverName() {
        return giverName;
    }

    public void setGiverName(String giverName) {
        this.giverName = giverName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSeizedCount() {
        return seizedCount;
    }

    public void setSeizedCount(int seizedCount) {
        this.seizedCount = seizedCount;
    }
}
