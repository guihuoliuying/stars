package com.stars.modules.buddy.summary;

import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyStageVo;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 伙伴常用数据
 * Created by liuyuheng on 2016/10/10.
 */
public class BuddySummaryComponentImpl extends AbstractSummaryComponent implements BuddySummaryComponent {
    private Map<Integer, BuddySummaryVo> buddySummaryMap;// 角色伙伴

    public BuddySummaryComponentImpl() {
    }

    public BuddySummaryComponentImpl(Map<Integer, RoleBuddy> roleBuddyMap) {
        if (roleBuddyMap == null)
            return;
        buddySummaryMap = new HashMap();
        for(RoleBuddy roleBuddy:roleBuddyMap.values()){
            if(roleBuddy == null) continue;
            buddySummaryMap.put(roleBuddy.getBuddyId(), new BuddySummaryVo(roleBuddy));
        }
    }

    @Override
    public String getName() {
        return "buddy";
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
        if(buddySummaryMap==null) return "";
        return StringUtil.makeString2(buddySummaryMap, '=', ',');
    }

    private void parseVer1(String str) throws Exception {
        buddySummaryMap = new HashMap<>();
        if (StringUtil.isEmpty(str)) return;
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');

        BuddySummaryVo buddySummaryVo;
        BuddyStageVo buddyStageVo;
        for(Map.Entry<String,String> entry:map.entrySet()){
            buddySummaryVo = new BuddySummaryVo(entry.getValue());
            buddyStageVo = BuddyManager.getBuddyStageVo(buddySummaryVo.getBuddyId(),buddySummaryVo.getStageLevel());
            if(buddyStageVo==null) continue;
            buddySummaryMap.put(Integer.parseInt(entry.getKey()),buddySummaryVo);
        }

//        boolean isOldData = false;
//
//        for(Map.Entry<String,String> entry:map.entrySet()){
//            if(StringUtil.isNumeric(entry.getKey())) {
//                buddySummaryMap.put(Integer.parseInt(entry.getKey()),
//                        new BuddySummaryVo(entry.getValue()));
//            }else{
//                isOldData = true;
//                break;
//            }
//        }
//        if(isOldData){
//            BuddySummaryVo bsv = new BuddySummaryVo(str);
//            buddySummaryMap.put(bsv.getBuddyId(),bsv);
//        }
    }

    @Override
    public Map<Integer, BuddySummaryVo> getBuddySummaryVoMap(){  return buddySummaryMap; }

    @Override
    public BuddySummaryVo getBuddySummaryVo(int buddyId){
        if(buddySummaryMap == null) return null;
        return buddySummaryMap.get(buddyId);
    }

    @Override
    public BuddySummaryVo getFightBuddySummaryVo() {
        if(buddySummaryMap == null) return null;
        for (BuddySummaryVo vo:buddySummaryMap.values()){
            if(vo!=null && vo.getIsFight() == 1) return vo;
        }
        return null;
    }
}
