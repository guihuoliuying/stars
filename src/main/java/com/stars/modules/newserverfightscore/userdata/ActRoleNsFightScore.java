package com.stars.modules.newserverfightscore.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liuyuheng on 2017/1/6.
 */
public class ActRoleNsFightScore extends DbRow {
    private long roleId;// '角色id'
    private int operateActId;// '活动id'
    private String rewardRecord;// '领奖记录',格式:rewardId,rewardId

    /* 内存数据 */
    private Set<Integer> recordSet = new HashSet<>();

    public ActRoleNsFightScore() {
    }

    public ActRoleNsFightScore(long roleId, int operateActId) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.rewardRecord = "";
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actnewserverfightscore", " `roleid`=" + roleId + " and `operateactid`="
                + operateActId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actnewserverfightscore", " `roleid`=" + roleId + " and `operateactid`=" + operateActId);
    }

    public boolean isRewarded(int rewardId) {
        return recordSet.contains(rewardId);
    }

    public void updateRewardRecord(int rewardId) {
        recordSet.add(rewardId);
        StringBuilder builder = new StringBuilder("");
        for (int recordId : recordSet) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(recordId);
        }
        this.rewardRecord = builder.toString();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int operateActId) {
        this.operateActId = operateActId;
    }

    public String getRewardRecord() {
        return rewardRecord;
    }

    public void setRewardRecord(String rewardRecord) throws Exception {
        this.rewardRecord = rewardRecord;
        if (StringUtil.isEmpty(rewardRecord) || "0".equals(rewardRecord)) {
            return;
        }
        recordSet.addAll(StringUtil.toArrayList(rewardRecord, Integer.class, ','));
    }
}
