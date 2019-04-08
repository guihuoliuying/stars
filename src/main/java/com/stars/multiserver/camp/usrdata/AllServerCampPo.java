package com.stars.multiserver.camp.usrdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampAtrVo;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.prodata.CampLevelVo;
import com.stars.modules.camp.prodata.CommonOfficerVo;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by huwenjun on 2017/6/28.
 */
public class AllServerCampPo extends DbRow {
    private int campType;
    private long prosperousnum;//繁荣度
    private int level;
    private transient List<CampCityVo> allCampCityList;
    private transient List<CampCityVo> openedCampCityList;
    private transient List<CommonOfficerVo> openedCommonOfficerList;
    private int roleNum;
    private String openedCityIds = "";//开放的城池id,+号分隔
    private String openedofficerids = "";//开放的普通官职id,+号分隔

    public AllServerCampPo(Integer campType, long prosperousnum, int level, int roleNum) {
        this.campType = campType;
        this.prosperousnum = prosperousnum;
        this.level = level;
        this.roleNum = roleNum;
        initOpenedCities();
    }

    public void initOpenedCities() {
        this.setOpenedCampCityList(CampManager.getOpenedCities(campType, level));
        this.setOpenedCommonOfficerList(CampManager.getOpenedOfficer(level));
    }

    public AllServerCampPo() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "allservercamp", "camptype=" + campType);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("allservercamp", "camptype=" + campType);
    }

    public Integer getCampType() {
        return campType;
    }

    public void setCampType(Integer campType) {
        this.campType = campType;
    }

    public long getProsperousnum() {
        return prosperousnum;
    }

    public void setProsperousnum(long prosperousnum) {
        this.prosperousnum = prosperousnum;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public CampLevelVo getCampLevelVo() {
        return CampManager.getCampLevelVo(campType, level);
    }

    public List<CampCityVo> getCampCityVoList() {
        if (allCampCityList == null || allCampCityList.size() == 0) {
            allCampCityList = CampManager.getAllCampCityList(campType);
        }
        return allCampCityList;
    }

    public int getRoleNum() {
        return roleNum;
    }

    public void setRoleNum(int roleNum) {
        this.roleNum = roleNum;
    }

    public CampAtrVo getCampAtrVo() {
        return CampManager.campAtrMap.get(campType);
    }

    public boolean addProsperousNum(long prosperousnum) {
        boolean canLevelUp = false;
        this.prosperousnum += prosperousnum;
        CampLevelVo campLevelVo = CampManager.getCampLevelVo(campType, level);
        if (this.prosperousnum >= campLevelVo.getReqlevel()) {
            canLevelUp = true;
            CampLevelVo nextLevelCampLevelVo = campLevelVo.getNextLevelCampLevelVo();
            if (nextLevelCampLevelVo == null) {
                return false;
            }
            this.level = nextLevelCampLevelVo.getLevel();
        }
        if (canLevelUp) {
            initOpenedCities();
        }
        return canLevelUp;
    }

    public String getOpenedCityIds() {
        if (StringUtil.isEmpty(openedCityIds)) {
            initOpenedCities();
        }
        return openedCityIds;
    }

    public List<Integer> getOpenCityIdList() {
        try {

            return StringUtil.toArrayList(getOpenedCityIds(), Integer.class, '+');
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void setOpenedCityIds(String openedCityIds) {
        this.openedCityIds = openedCityIds;
        openedCampCityList = new ArrayList<>();
        try {
            List<Integer> cityIdList = StringUtil.toArrayList(openedCityIds, Integer.class, '+');
            for (Integer cityId : cityIdList) {
                openedCampCityList.add(CampManager.campCityMap.get(cityId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getOpenedofficerids() {
        if (StringUtil.isEmpty(openedofficerids)) {
            initOpenedCities();
        }
        return openedofficerids;
    }

    public void setOpenedofficerids(String openedofficerids) {
        this.openedofficerids = openedofficerids;
        openedCommonOfficerList = new ArrayList<>();
        try {
            List<Integer> cityIdList = StringUtil.toArrayList(getOpenedCityIds(), Integer.class, '+');
            for (Integer cityId : cityIdList) {
                openedCommonOfficerList.add(CampManager.commonOfficerMap.get(cityId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CampCityVo> getOpenedCampCityList() {
        if (openedCampCityList == null) {
            openedCampCityList = new ArrayList<>();
            try {
                List<Integer> openedCityList = StringUtil.toArrayList(getOpenedCityIds(), Integer.class, '+');
                for (Integer cityId : openedCityList) {
                    openedCampCityList.add(CampManager.campCityMap.get(cityId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Collections.sort(openedCampCityList);
        }
        return openedCampCityList;
    }

    public void setOpenedCampCityList(List<CampCityVo> openedCampCityList) {
        this.openedCampCityList = openedCampCityList;
        Collections.sort(this.openedCampCityList);
        StringBuilder tmpOpenedCityIds = new StringBuilder();
        if (openedCampCityList.size() > 0) {
            tmpOpenedCityIds.append(openedCampCityList.get(0).getId());
            for (int index = 1; index < openedCampCityList.size(); index++) {
                tmpOpenedCityIds.append("+" + openedCampCityList.get(index).getId());
            }
            openedCityIds = tmpOpenedCityIds.toString();
        }

    }

    public List<CommonOfficerVo> getOpenedCommonOfficerList() {
        if (openedCommonOfficerList == null) {
            openedCommonOfficerList = new ArrayList<>();
            try {
                List<Integer> commonOfficerIds = StringUtil.toArrayList(getOpenedofficerids(), Integer.class, '+');
                for (Integer commonOfficerId : commonOfficerIds) {
                    CommonOfficerVo commonOfficerVo = CampManager.commonOfficerMap.get(commonOfficerId);
                    openedCommonOfficerList.add(commonOfficerVo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return openedCommonOfficerList;
    }

    public void setOpenedCommonOfficerList(List<CommonOfficerVo> openedCommonOfficerList) {
        this.openedCommonOfficerList = openedCommonOfficerList;
        StringBuilder tmpOpenedOfficerIds = new StringBuilder();
        if (openedCommonOfficerList.size() > 0) {
            tmpOpenedOfficerIds.append(openedCommonOfficerList.get(0).getId());
            for (int index = 1; index < openedCommonOfficerList.size(); index++) {
                tmpOpenedOfficerIds.append("+" + openedCommonOfficerList.get(index).getId());
            }
            openedofficerids = tmpOpenedOfficerIds.toString();
        }
    }
}
