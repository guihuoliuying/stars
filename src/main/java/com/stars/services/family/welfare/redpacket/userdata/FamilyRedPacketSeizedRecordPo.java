package com.stars.services.family.welfare.redpacket.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public class FamilyRedPacketSeizedRecordPo extends DbRow {

    private long familyId; // 家族id
    private long redPacketId; // 红包id
    private long seizerId; // 抢红包者id
    private String seizerName; // 抢红包者名字
    private int timestamp; // 时间戳
    private Map<Integer, Integer> seizedToolMap; // 战利品

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyredpacketseizedrecord", "`familyid`=" + familyId + " and `redpacketid`=" + redPacketId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyredpacketseizedrecord` where `familyid`=" + familyId + " and `redpacketid`=" + redPacketId;
    }

    /* Db Data Getter/Setter */
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

    public long getSeizerId() {
        return seizerId;
    }

    public void setSeizerId(long seizerId) {
        this.seizerId = seizerId;
    }

    public String getSeizerName() {
        return seizerName;
    }

    public void setSeizerName(String seizerName) {
        this.seizerName = seizerName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public Map<Integer, Integer> getSeizedToolMap() {
        return seizedToolMap;
    }

    public void setSeizedToolMap(Map<Integer, Integer> seizedToolMap) {
        this.seizedToolMap = seizedToolMap;
    }

    public void setSeizedTool(String seizedTool) throws Exception {
        this.seizedToolMap = StringUtil.toMap(seizedTool, Integer.class, Integer.class, '+', '|');
    }

    public String getSeizedTool() {
        return StringUtil.makeString(seizedToolMap, '+', '|');
    }
}
