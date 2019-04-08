package com.stars.services.newofflinepvp.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-03-08 15:47
 */
public class NewOfflinePvpRankPo extends DbRow implements Comparable<NewOfflinePvpRankPo>, Cloneable {
    private long roleId;
    private int rank;
    private int jobId;
    private int level;
    private String roleName;
    private int fightScore;
    private byte roleOrRobot;//1:role--0:robot
    private long lastFightTimestamp;
    private long lastFightObject;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public byte getRoleOrRobot() {
        return roleOrRobot;
    }

    public void setRoleOrRobot(byte roleOrRobot) {
        this.roleOrRobot = roleOrRobot;
    }

    public long getLastFightTimestamp() {
        return lastFightTimestamp;
    }

    public void setLastFightTimestamp(long lastFightTimestamp) {
        this.lastFightTimestamp = lastFightTimestamp;
    }

    public long getLastFightObject() {
        return lastFightObject;
    }

    public void setLastFightObject(long lastFightObject) {
        this.lastFightObject = lastFightObject;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(rank);
        buff.writeString(String.valueOf(roleId));
        buff.writeString(roleName);
        buff.writeInt(fightScore);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "offlinepvprank", " `roleid`=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from offlinepvprank where `roleid`=" + this.roleId;
    }

    @Override
    public int compareTo(NewOfflinePvpRankPo o) {
        if (this.roleId == o.roleId) {
            return 0;
        } else {
            if (this.rank == o.rank) {
                return 0;
            } else {
                return this.rank < o.rank ? -1 : 1;
            }
        }
    }

    public NewOfflinePvpRankPo copy() {
        try {
            return (NewOfflinePvpRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("NewOfflinePvpRankPo克隆失败", e);
        }
        return null;
    }
}
