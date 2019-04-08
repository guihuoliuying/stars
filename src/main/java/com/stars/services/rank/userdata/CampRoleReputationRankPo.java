package com.stars.services.rank.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampRankAwardVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class CampRoleReputationRankPo extends AbstractRankPo implements Cloneable {
    private long roleId;
    private int serverId;
    private String name;
    private Integer jobId;
    private int campType;
    private int cityId;
    private long reputation;//声望
    private int fightScore;//战力
    private int commonOfficerId;//普通官职
    private int rareOfficerId;//稀有官职
    private int designateOfficerId;//任命官职

    public CampRoleReputationRankPo(long roleId, int serverId, String name, int jobId, int campType, int cityId, long reputation, int fightScore, int commonOfficerId, int rareOfficerId, int designateOfficerId) {
        this.roleId = roleId;
        this.serverId = serverId;
        this.name = name;
        this.jobId = jobId;
        this.campType = campType;
        this.cityId = cityId;
        this.reputation = reputation;
        this.fightScore = fightScore;
        this.commonOfficerId = commonOfficerId;
        this.rareOfficerId = rareOfficerId;
        this.designateOfficerId = designateOfficerId;
    }

    public CampRoleReputationRankPo(long roleId, String name, Integer jobId, int campType, int cityId, long reputation, int fightScore, int commonOfficerId, int rareOfficerId, int designateOfficerId) {
        this.roleId = roleId;
        this.name = name;
        this.jobId = jobId;
        this.campType = campType;
        this.cityId = cityId;
        this.reputation = reputation;
        this.fightScore = fightScore;
        this.commonOfficerId = commonOfficerId;
        this.rareOfficerId = rareOfficerId;
        this.designateOfficerId = designateOfficerId;
    }

    public CampRoleReputationRankPo() {
    }

    @Override
    public long getUniqueId() {
        return roleId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {
        buff.writeString(roleId + "");
        buff.writeString(name);
        buff.writeInt(campType);
        buff.writeLong(reputation);
        buff.writeInt(commonOfficerId);
        buff.writeInt(rareOfficerId);
        buff.writeInt(designateOfficerId);
        buff.writeInt(fightScore);
        buff.writeInt(getRank());
        CampRankAwardVo campRankAward = CampManager.getCampRankAward(cityId, getRank());
        buff.writeString(campRankAward.getShowaward());
    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolereputationrank", "roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolereputationrank", "roleid=" + roleId);
    }

    @Override
    public int compareTo(Object o) {
        CampRoleReputationRankPo campRoleReputationRankPo = (CampRoleReputationRankPo) o;
        int differ = (int) (campRoleReputationRankPo.getReputation() - this.getReputation());
        if (differ != 0) {
            return differ;
        } else {
            differ = campRoleReputationRankPo.getCommonOfficerId() - this.getCommonOfficerId();
            if (differ != 0) {
                return differ;
            } else {
                differ = campRoleReputationRankPo.getFightScore() - this.getFightScore();
                if (differ != 0) {
                    return differ;
                } else {
                    return (int) (campRoleReputationRankPo.getRoleId() - this.getRoleId());
                }
            }
        }
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCampType() {
        return campType;
    }

    public void setCampType(int campType) {
        this.campType = campType;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public long getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void setReputation(long reputation) {
        this.reputation = reputation;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getCommonOfficerId() {
        return commonOfficerId;
    }

    public void setCommonOfficerId(int commonOfficerId) {
        this.commonOfficerId = commonOfficerId;
    }

    public int getRareOfficerId() {
        return rareOfficerId;
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public int getDesignateOfficerId() {
        return designateOfficerId;
    }

    public void setDesignateOfficerId(int designateOfficerId) {
        this.designateOfficerId = designateOfficerId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }
}
