package com.stars.services.rank.userdata;

import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-02-27 10:34
 */
public class FamilyTreasureRankPo extends AbstractRankPo {
    private long familyId; // 家族id
    private String name; // 家族名
    private String masterName; // 族长名字
    private int level;//探宝阶层
    private int step;//探宝步数
    private long totalDamage;//最后领头伤害

    private boolean over;//是否全部打完

    public FamilyTreasureRankPo() {
    }

    public FamilyTreasureRankPo(long familyId) {
        this.familyId = familyId;
    }

    public FamilyTreasureRankPo(long familyId, String name, String masterName, int level, int step, long totalDamage, boolean over) {
        this.familyId = familyId;
        this.name = name;
        this.masterName = masterName;
        this.level = level;
        this.step = step;
        this.totalDamage = totalDamage;
        this.over = over;
    }

    @Override
    public long getUniqueId() {
        return familyId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {
        buff.writeInt(getRank());
        buff.writeString(Long.toString(getUniqueId()));
        buff.writeString(name);
        buff.writeString(masterName);
        buff.writeString(over ? level + "-" + step : level + "-" + (step - 1));
        buff.writeLong(totalDamage);
    }

    @Override
    public int hashCode() {
        return (int) (familyId ^ (familyId >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FamilyTreasureRankPo that = (FamilyTreasureRankPo) o;
        if (familyId != that.familyId) return false;
        return true;
    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("FamilyTreasureRankPo克隆失败", e);
        }
        return null;
    }

    @Override
    public int compareTo(Object o) {
        return compareByFamilyTreasure((FamilyTreasureRankPo) o);
    }

    private int compareByFamilyTreasure(FamilyTreasureRankPo other) {
        if (this.familyId == other.familyId) {
            return 0;
        }
        if (this.level == other.level) {
            if (this.step == other.step) {
                return this.totalDamage - other.totalDamage > 0 ? -1 : 1;
            } else {
                return this.step - other.step > 0 ? -1 : 1;
            }
        } else {
            return this.level - other.level > 0 ? -1 : 1;
        }
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rankfamilytreasure", "`familyid`=" + this.familyId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rankfamilytreasure` where `familyid`=" + this.familyId;
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }
}
