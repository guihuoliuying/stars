package com.stars.multiserver.camp.pojo;


import com.stars.modules.camp.pojo.CampFightGrowUP;
import com.stars.modules.scene.fightdata.FighterEntity;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class CampFightMatchInfo {
    private long roleId;
    private int campType;
    private int fromServerId;
    private long beginTime;
    private int takeSingleRewardTime;
    private FighterEntity campFightEntity;
    private CampFightGrowUP campFightGrowUP;

    public CampFightMatchInfo() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getCampType() {
        return campType;
    }

    public void setCampType(int campType) {
        this.campType = campType;
    }

    public int getFromServerId() {
        return fromServerId;
    }

    public void setFromServerId(int fromServerId) {
        this.fromServerId = fromServerId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public FighterEntity getCampFightEntity() {
        return campFightEntity.copy();
    }

    public void setCampFightEntity(FighterEntity campFightEntity) {
        this.campFightEntity = campFightEntity;
        this.roleId = Long.parseLong(campFightEntity.getUniqueId());
    }

    public CampFightGrowUP getCampFightGrowUP() {
        return (CampFightGrowUP) campFightGrowUP.clone();
    }

    public void setCampFightGrowUP(CampFightGrowUP campFightGrowUP) {
        this.campFightGrowUP = campFightGrowUP;
    }

    public int getTakeSingleRewardTime() {
        return takeSingleRewardTime;
    }

    public void setTakeSingleRewardTime(int takeSingleRewardTime) {
        this.takeSingleRewardTime = takeSingleRewardTime;
    }

    public void addTakeSingleRewardTime() {
        this.takeSingleRewardTime += 1;
    }

}
