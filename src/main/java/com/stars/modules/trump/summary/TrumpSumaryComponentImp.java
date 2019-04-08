package com.stars.modules.trump.summary;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/14.
 */
public class TrumpSumaryComponentImp extends AbstractSummaryComponent implements TrumpSummaryComponent {
    private Map<Integer, String> skillDamage = new HashMap<>();
    private Map<Integer, Byte> putOn = new HashMap<>();

    public TrumpSumaryComponentImp(){}

    public TrumpSumaryComponentImp(Map<Integer, String> skillDamage, Map<Integer, Byte> putOn) {
        this.skillDamage = skillDamage;
        this.putOn = putOn;
    }

    @Override
    public String getName() {
        return "trump";
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
        com.stars.util.MapUtil.setString(map, "putOn", StringUtil.makeString2(putOn, '=', ','));
        com.stars.util.MapUtil.setString(map, "skillDamage", StringUtil.makeString2(skillDamage, '=', ','));
        return StringUtil.makeString2(map, '=',',');
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        putOn = StringUtil.toMap(com.stars.util.MapUtil.getString(map, "putOn", ""), Integer.class, Byte.class, '=', ',');
        skillDamage = StringUtil.toMap(MapUtil.getString(map, "skillDamage", ""), Integer.class, String.class, '=', ',');
    }

    @Override
    public Map<Integer, String> getSkillDamageMap() {
        return skillDamage;
    }

    @Override
    public Map<Integer, Byte> getPutOnMap() {
        return putOn;
    }
}
