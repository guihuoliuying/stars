package com.stars.modules.family.summary;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class FamilySummaryComponentImpl extends AbstractSummaryComponent implements FamilySummaryComponent {

    private long familyId;
    private String familyName;
    private byte postId;
    private Map<String, Integer> skillLevelMap; // 家族心法等级表

    public FamilySummaryComponentImpl() {
    }

    public FamilySummaryComponentImpl(long familyId, String familyName, byte postId,Map<String, Integer> skillLevelMap) {
        this.familyId = familyId;
        this.familyName = familyName;
        this.postId = postId;
        this.skillLevelMap = new HashMap<>(skillLevelMap);
    }

    @Override
    public String getName() {
        return "family";
    }

    @Override
    public int getLatestVersion() {
        return 1;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setLong(map, "familyId", familyId);
        com.stars.util.MapUtil.setString(map, "familyName", familyName);
        com.stars.util.MapUtil.setByte(map, "postId", postId);
        com.stars.util.MapUtil.setString(map, "skillLevelMap", StringUtil.makeString2(skillLevelMap, '=', ','));
        return StringUtil.makeString2(map, '=', ',');
    }

    @Override
    public long getFamilyId() {
        return familyId;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public byte getPostId() {
        return postId;
    }

    @Override
    public Map<String, Integer> getSkillLevelMap() {
        return skillLevelMap;
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        familyId = com.stars.util.MapUtil.getLong(map, "familyId", 0L);
        familyName = com.stars.util.MapUtil.getString(map, "familyName", "");
        postId = com.stars.util.MapUtil.getByte(map, "postId", (byte) 0);
        skillLevelMap = StringUtil.toMap(MapUtil.getString(map, "skillLevelMap", ""), String.class, Integer.class, '=', ',');
    }

}
