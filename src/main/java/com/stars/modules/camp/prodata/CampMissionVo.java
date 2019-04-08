package com.stars.modules.camp.prodata;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.modules.role.userdata.Role;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class CampMissionVo implements Cloneable, Comparable<CampMissionVo> {
    private Integer id;//任务id
    private String name;//任务名称
    private String desc;//任务描述
    private String image;//任务图片icon
    private Integer rank;//排序字段
    private Integer type;//任务类型
    private String target;//任务目标
    private String award;//任务奖励
    private String camplevel;//阵营等级
    private String officer;//官职限制
    private String level;

    private Integer roleLevelMin;//角色等级最小值
    private Integer roleLevelMax;//角色等级最大值
    private Integer campLevelMin;//阵营最小等级
    private Integer campLevelMax;//阵营最大等级
    private List<OfficerLimitGroup> officerLimitGroups;
    private Map<Integer, Integer> reward;
    private int targetId;
    private int targetTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
        try {
            Integer[] group = StringUtil.toArray(target, Integer[].class, '=');
            targetId = group[0];
            targetTime = group[1];
        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        reward = StringUtil.toMap(award, Integer.class, Integer.class, '=', '|');
    }

    public String getCamplevel() {
        return camplevel;
    }

    public void setCamplevel(String camplevel) {
        this.camplevel = camplevel;
        try {
            Integer[] levelGroup = StringUtil.toArray(camplevel, Integer[].class, '+');
            campLevelMin = levelGroup[0];
            campLevelMax = levelGroup[1];
        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }

    }

    public boolean isComplete(RoleCampTimesPo roleCampTimesPo) {
        if (roleCampTimesPo.getJoinTimesByMisId(id) >= targetTime) {
            return true;
        }
        return false;
    }


    public String getOfficer() {
        return officer;
    }

    public void setOfficer(String officer) {
        this.officer = officer;
        officerLimitGroups = new ArrayList<>();
        String[] groups = officer.split("\\|");
        for (String group : groups) {
            OfficerLimitGroup officerLimitGroup = OfficerLimitGroup.parse(group);
            officerLimitGroups.add(officerLimitGroup);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(CampMissionVo o) {
        return o.getRank() - this.getRank();
    }

    public boolean canJoin(RoleCampPo roleCamp, AllServerCampPo allServerCampPo, Role role) {
        for (OfficerLimitGroup group : officerLimitGroups) {
            if (!group.check(roleCamp)) {
                return false;
            }
        }
        if (!(campLevelMax >= allServerCampPo.getLevel() && campLevelMin <= allServerCampPo.getLevel())) {
            return false;
        }
        if (!(roleLevelMax >= role.getLevel() && roleLevelMin <= role.getLevel())) {
            return false;
        }
        return true;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(int targetTime) {
        this.targetTime = targetTime;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        try {
            Integer[] group = StringUtil.toArray(level, Integer[].class, '+');
            roleLevelMin = group[0];
            roleLevelMax = group[1];
        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeString(name);
        buff.writeString(desc);
        buff.writeString(image);
        buff.writeInt(rank);
        buff.writeInt(type);
        buff.writeString(target);
        buff.writeString(camplevel);
        buff.writeString(officer);
        buff.writeString(award);
        if (type == 1) {//活动需要下发活动的windowid
            CampActivityVo campActivityVo = CampManager.campActivityMap.get(targetId);
            buff.writeString(campActivityVo.getOpenWindow());
        } else {
            buff.writeString("");
        }
    }
}

class OfficerLimitGroup {
    private int type;
    private int minLv;
    private int maxLv;

    private OfficerLimitGroup() {
    }

    public static OfficerLimitGroup parse(String group) {
        OfficerLimitGroup officerLimitGroup = new OfficerLimitGroup();
        Integer[] paramsInt = null;
        try {
            paramsInt = StringUtil.toArray(group, Integer[].class, '+');
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        officerLimitGroup.setType(paramsInt[0]);
        officerLimitGroup.setMinLv(paramsInt[1]);
        officerLimitGroup.setMaxLv(paramsInt[2]);
        return officerLimitGroup;
    }

    public boolean check(RoleCampPo roleCampPo) {
        int lv = 0;
        switch (type) {
            case 1: {
                lv = roleCampPo.getCommonOfficer().getLevel();
            }
            break;
            case 2: {
                lv = roleCampPo.getRareOfficer().getQuality();
            }
            break;
            case 3: {
                lv = roleCampPo.getDesignateOfficer().getQuality();
            }
            break;
        }
        return lv <= maxLv && lv >= minLv;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMinLv() {
        return minLv;
    }

    public void setMinLv(int minLv) {
        this.minLv = minLv;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }

}