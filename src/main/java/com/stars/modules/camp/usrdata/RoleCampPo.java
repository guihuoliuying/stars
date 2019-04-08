package com.stars.modules.camp.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.prodata.CommonOfficerVo;
import com.stars.modules.camp.prodata.DesignateOfficerVo;
import com.stars.modules.camp.prodata.RareOfficerVo;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class RoleCampPo extends DbRow {
    private Long roleId;
    private int campType;
    private int commonOfficerId;
    private int rareOfficerId;
    private int designateOfficerId;
    private int cityId;


    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolecamp", " roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolecamp", " roleid=" + roleId);
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getCampType() {
        return campType;
    }

    public void setCampType(Integer campType) {
        this.campType = campType;
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

    public RareOfficerVo getRareOfficer() {
        return CampManager.rareOfficerMap.get(rareOfficerId);
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public CampCityVo getCampCity() {
        return CampManager.campCityMap.get(cityId);
    }

    public CommonOfficerVo getCommonOfficer() {
        return CampManager.commonOfficerMap.get(commonOfficerId);
    }

    public int getDesignateOfficerId() {
        return designateOfficerId;
    }

    public DesignateOfficerVo getDesignateOfficer() {
        return CampManager.designateOfficerMap.get(designateOfficerId);
    }

    public void setDesignateOfficerId(int designateOfficerId) {
        this.designateOfficerId = designateOfficerId;
    }


}
