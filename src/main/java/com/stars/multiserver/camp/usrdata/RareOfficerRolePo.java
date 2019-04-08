package com.stars.multiserver.camp.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.RareOfficerVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class RareOfficerRolePo extends DbRow implements Comparable<RareOfficerRolePo> {
    private long roleId;
    private int serverId;
    private String name;
    private int jobId;
    private int campType;
    private int cityId;
    private long reputation;//声望
    private int rareOfficerId;//稀有官职id
    private int flowerNum;//被送花数
    private int rank;

    public RareOfficerRolePo() {
    }

    public RareOfficerRolePo(CampRoleReputationRankPo campRoleReputationRankPo, int rareOfficerId, int flowerNum) {
        this.roleId = campRoleReputationRankPo.getRoleId();
        this.serverId = campRoleReputationRankPo.getServerId();
        this.name = campRoleReputationRankPo.getName();
        this.campType = campRoleReputationRankPo.getCampType();
        this.cityId = campRoleReputationRankPo.getCityId();
        this.jobId = campRoleReputationRankPo.getJobId();
        this.reputation = campRoleReputationRankPo.getReputation();
        this.rareOfficerId = rareOfficerId;
        this.flowerNum = flowerNum;
        this.rank = campRoleReputationRankPo.getRank();
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

    public void setReputation(long reputation) {
        this.reputation = reputation;
    }

    public int getRareOfficerId() {
        return rareOfficerId;
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public int getFlowerNum() {
        return flowerNum;
    }

    public void setFlowerNum(int flowerNum) {
        this.flowerNum = flowerNum;
    }


    public RareOfficerVo getRareOfficer() {
        return CampManager.rareOfficerMap.get(rareOfficerId);
    }

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

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "rareofficerrole", "roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rareofficerrole", "roleid=" + roleId);
    }

    @Override
    public int compareTo(RareOfficerRolePo o) {
        return this.getRank() - o.getRank();
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeString(roleId + "");
        buff.writeString(getName());
        buff.writeInt(jobId);
        buff.writeInt(rareOfficerId);
        buff.writeString(MultiServerHelper.getServerName(getServerId()));
        buff.writeInt(getFlowerNum());
    }
}
