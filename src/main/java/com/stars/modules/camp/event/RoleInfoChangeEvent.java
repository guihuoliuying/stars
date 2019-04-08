package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/7/10.
 */
public class RoleInfoChangeEvent extends Event {
    private long roleId;
    private String newName;
    private Integer newJobId;
    private int cityId;
    private int commonOfficerId;
    private int rareOfficerId;
    private int designateOfficerId;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Integer getNewJobId() {
        return newJobId;
    }

    public void setNewJobId(Integer newJobId) {
        this.newJobId = newJobId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
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
}
