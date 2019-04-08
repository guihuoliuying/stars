package com.stars.services.rank.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.RankConstant;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/11/30.
 */
public class FamilyRankPo extends AbstractRankPo {

    private long familyId; // 家族id
    private String name; // 家族名
    private String masterName; // 族长名字
    private int level; // 家族等级
    private long fightScore; // 战力

    public FamilyRankPo() {
    }

    public FamilyRankPo(long familyId) {
        this.familyId = familyId;
    }

    public FamilyRankPo(long familyId, String name, String masterName, int level, long fightScore) {
        this.familyId = familyId;
        this.name = name;
        this.masterName = masterName;
        this.level = level;
        this.fightScore = fightScore;
    }

    @Override
    public long getUniqueId() {
        return familyId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {
        buff.writeInt(getRank()); // 排名
        buff.writeString(Long.toString(getUniqueId())); // 家族id
        buff.writeString(name); // 家族名
        buff.writeString(masterName); // 族长名字
        buff.writeInt(level); // 家族等级
        buff.writeString(Long.toString(fightScore)); // 战力
    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("FamilyRankPo克隆失败", e);
        }
        return null;
    }

    @Override
    public int compareTo(Object o) {
        switch (getRankId()) {
            case RankConstant.RANKID_FAMILYFIGHTSCORE:
                return compareByFamilyFightScore((FamilyRankPo) o);
        }
        return 0;
    }

    private int compareByFamilyFightScore(FamilyRankPo other) {
        if (this.familyId == other.familyId) {
            return 0;
        }
        if (this.fightScore == other.fightScore) {
            if (this.level == other.level) {
                return this.familyId - other.familyId > 0 ? -1 : 1;
            } else {
                return this.level - other.level > 0 ? -1 : 1;
            }
        } else {
            return this.fightScore - other.fightScore > 0 ? -1 : 1;
        }
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rankfamilyfightscore", "`familyid`=" + familyId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rankfamilyfightscore` where `familyid`=" + familyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FamilyRankPo that = (FamilyRankPo) o;
        return familyId == that.familyId;
    }

    @Override
    public int hashCode() {
        return (int) (familyId ^ (familyId >>> 32));
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getFightScore() {
        return fightScore;
    }

    public void setFightScore(long fightScore) {
        this.fightScore = fightScore;
    }

    @Override
    public String toString() {
        return "(" + familyId +
                ", " + name +
                ", " + masterName +
                ", " + level +
                ", " + fightScore +
                ')';
    }
}
