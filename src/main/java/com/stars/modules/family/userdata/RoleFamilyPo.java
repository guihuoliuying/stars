package com.stars.modules.family.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/12.
 */
public class RoleFamilyPo extends DbRow {

    private long roleId;
    private byte donateResidue;
    private byte rmbDonateResidue;
    private String skillLevels;
    private String familyTaskStr;//个人家族任务信息

    private Map<String, Integer> skillLevelMap; // 家族等级哈希表
    private Map<Integer, Byte> familyTaskMap;//个人家族任务集合    key：任务id value: 任务状态（0：未提交（未求助）；1：求助中；2：已提交）
    private int missionId;
    private byte familyTaskAward;//奖励      0 未领取          1 已领取
    private byte askHelpTimes;//今天已求助次数

    public RoleFamilyPo() {
        this.skillLevelMap = new HashMap<>();
        this.familyTaskMap = new HashMap<>();
    }

    public RoleFamilyPo(long roleId, byte donateLimit, byte rmbDonateLimit) {
        this.roleId = roleId;
        this.donateResidue = donateLimit;
        this.rmbDonateResidue = rmbDonateLimit;
        this.skillLevelMap = new HashMap<>();
        this.familyTaskMap = new HashMap<>();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolefamily", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rolefamily` where `roleid`=" + roleId;
    }

    /* Mem Data Getter/Setter */
    public Map<String, Integer> getSkillLevelMap() {
        return skillLevelMap;
    }

    public void setSkillLevelMap(Map<String, Integer> skillLevelMap) {
        this.skillLevelMap = skillLevelMap;
    }

    public Map<Integer, Byte> getFamilyTaskMap() {
		return familyTaskMap;
	}

	public void setFamilyTaskMap(Map<Integer, Byte> familyTaskMap) {
		this.familyTaskMap = familyTaskMap;
	}

	/* Db Data Getter/Setter */
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public byte getDonateResidue() {
        return donateResidue;
    }

    public void setDonateResidue(byte donateResidue) {
        this.donateResidue = donateResidue;
    }

    public byte getRmbDonateResidue() {
        return rmbDonateResidue;
    }

    public void setRmbDonateResidue(byte rmbDonateResidue) {
        this.rmbDonateResidue = rmbDonateResidue;
    }

    public String getSkillLevels() {
        return StringUtil.makeString(skillLevelMap, '+', '|');
    }

    public void setSkillLevels(String skillLevels) throws Exception {
        this.skillLevelMap = StringUtil.toMap(skillLevels, String.class, Integer.class, '+', '|');
    }

	public String getFamilyTaskStr() {
		return StringUtil.makeString(familyTaskMap, '+', '|');
	}

	public void setFamilyTaskStr(String familyTaskStr) {
		this.familyTaskMap = StringUtil.toMap(familyTaskStr, Integer.class, Byte.class, '+', '|');;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public byte getFamilyTaskAward() {
		return familyTaskAward;
	}

	public void setFamilyTaskAward(byte familyTaskAward) {
		this.familyTaskAward = familyTaskAward;
	}

	public byte getAskHelpTimes() {
		return askHelpTimes;
	}

	public void setAskHelpTimes(byte askHelpTimes) {
		this.askHelpTimes = askHelpTimes;
	}
}
