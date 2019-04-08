package com.stars.modules.role.summary;

import com.stars.core.attr.Attribute;
import com.stars.modules.MConst;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public class RoleSummaryComponentImpl extends AbstractSummaryComponent implements RoleSummaryComponent {

    private long roleId;
    private String name;
    private int level;
    private int jobId;
    private int fightScore;
    private int titleId;
    private int vigour;
    private long createTime;
    private int channel;
    private Attribute totalAttr;
    private Map<String, Integer> fightScoreMap;
    private int campType;

    public RoleSummaryComponentImpl() {
    }

    public RoleSummaryComponentImpl(long roleId, String name, int level, int jobId, int fightScore,
                                    int vigour, int titleId, long createTime, int channel, Attribute totalAttr, 
                                    Map<String, Integer> fightScoreMap, int campType) {
        this.roleId = roleId;
        this.name = name;
        this.level = level;
        this.jobId = jobId;
        this.fightScore = fightScore;
        this.titleId = titleId;
        this.vigour = vigour;
        this.createTime = createTime;
        this.totalAttr = new Attribute(totalAttr);
        this.channel = channel;
//        this.fightScoreMap = Collections.unmodifiableMap(fightScoreMap);
        this.fightScoreMap = new HashMap<>(fightScoreMap);
        this.campType = campType;
    }

    @Override
    public String getName() {
        return MConst.Role;
    }

    @Override
    public int getLatestVersion() {
        return 2;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
                case 2:
                    parseVer2(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setLong(map, "roleId", roleId);
        com.stars.util.MapUtil.setString(map, "name", name);
        com.stars.util.MapUtil.setInt(map, "level", level);
        com.stars.util.MapUtil.setInt(map, "jobId", jobId);
        com.stars.util.MapUtil.setInt(map, "fightScore", fightScore);
        com.stars.util.MapUtil.setInt(map, "titleId", titleId);
        com.stars.util.MapUtil.setInt(map, "campType", campType);
        com.stars.util.MapUtil.setInt(map, "vigour", vigour);
        com.stars.util.MapUtil.setInt(map, "channel", channel);
        com.stars.util.MapUtil.setLong(map, "createTime", createTime);
        com.stars.util.MapUtil.setString(map, "totalAttr", "{" + totalAttr.getAttributeStr() + "}");
        com.stars.util.MapUtil.setString(map, "fightScoreMap", StringUtil.makeString2(fightScoreMap, '=', ','));
        return StringUtil.makeString2(map, '=', ',');
    }

    @Override
    public String getRoleName() {
        return name;
    }

    @Override
    public int getRoleLevel() {
        return level;
    }

    @Override
    public int getRoleJob() {
        return jobId;
    }

    @Override
    public int getFightScore() {
        return fightScore;
    }

    @Override
    public int getTitleId() {
        return titleId;
    }

    public int getCampType() {
		return campType;
	}

	@Override
    public int getVigour() {
        return vigour;
    }

    @Override
    public Attribute getTotalAttr() {
        return totalAttr;
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public void setChannel(int channel) {
        this.channel = channel;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    @Override
    public Map<String, Integer> getFightScoreMap() {
        return fightScoreMap;
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.roleId = com.stars.util.MapUtil.getLong(map, "roleId", 0L);
        this.name = com.stars.util.MapUtil.getString(map, "name", "");
        this.level = com.stars.util.MapUtil.getInt(map, "level", 1);
        this.jobId = com.stars.util.MapUtil.getInt(map, "jobId", 1);
        this.fightScore = com.stars.util.MapUtil.getInt(map, "fightScore", 1);
        this.titleId = com.stars.util.MapUtil.getInt(map, "titleId", 1);
        this.campType = com.stars.util.MapUtil.getInt(map, "campType", 0);
        this.vigour = com.stars.util.MapUtil.getInt(map, "vigour", 0);
        this.createTime = com.stars.util.MapUtil.getLong(map, "createTime", 0);
        String attributeString = com.stars.util.MapUtil.getString(map, "totalAttr", "");
        this.totalAttr = new Attribute(attributeString.substring(1, attributeString.length() - 1));
        this.fightScoreMap = StringUtil.toMap(com.stars.util.MapUtil.getString(map, "fightScoreMap", ""), String.class, Integer.class, '=', ',');
        if (fightScoreMap.containsKey("equip")) {
            fightScoreMap.put("equipment", fightScoreMap.get("equip"));
            fightScoreMap.remove("equip");
        }
        // 特殊处理
//        if (this.level > 100) {
//            this.level = 100;
//        }
    }

    private void parseVer2(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.roleId = com.stars.util.MapUtil.getLong(map, "roleId", 0L);
        this.name = com.stars.util.MapUtil.getString(map, "name", "");
        this.level = com.stars.util.MapUtil.getInt(map, "level", 1);
        this.jobId = com.stars.util.MapUtil.getInt(map, "jobId", 1);
        this.fightScore = com.stars.util.MapUtil.getInt(map, "fightScore", 1);
        this.titleId = com.stars.util.MapUtil.getInt(map, "titleId", 1);
        this.campType = com.stars.util.MapUtil.getInt(map, "campType", 0);
        this.vigour = com.stars.util.MapUtil.getInt(map, "vigour", 0);
        this.createTime = com.stars.util.MapUtil.getLong(map, "createTime", 0);
        String attributeString = com.stars.util.MapUtil.getString(map, "totalAttr", "");
        this.totalAttr = new Attribute(attributeString.substring(1, attributeString.length() - 1));
        this.fightScoreMap = StringUtil.toMap(com.stars.util.MapUtil.getString(map, "fightScoreMap", ""), String.class, Integer.class, '=', ',');
        if (fightScoreMap.containsKey("equip")) {
            fightScoreMap.put("equipment", fightScoreMap.get("equip"));
            fightScoreMap.remove("equip");
        }
//        // 特殊处理
//        if (this.level > 100) {
//            this.level = 100;
//        }
        this.channel = MapUtil.getInt(map, "channel", 0);
    }
}
