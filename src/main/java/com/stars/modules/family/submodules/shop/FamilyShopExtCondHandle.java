package com.stars.modules.family.submodules.shop;

import com.stars.core.module.Module;
import com.stars.modules.shop.ExtendConditionHandle;
import com.stars.modules.shop.prodata.Shop;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyShopExtCondHandle implements ExtendConditionHandle {

    private int reqFamilyLevel; // 家族等级
    private int reqMaxPassedExpeId; // 需要的最大远征等级

    @Override
    public void setShopVo(Shop item) {
//        if(item == null || StringUtil.isEmpty(item.getExtendCondition()) ||
//                item.getExtendCondition().equals("0")) return;
//        try {
//            int[] array = StringUtil.toArray(item.getExtendCondition(), int[].class, '+');
//            reqFamilyLevel = array[0];
//            reqMaxPassedExpeId = array[1];
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public boolean check(Map<String, Module> moduleMap) {
//        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
//        FamilyActExpeditionModule expeditionModule = (FamilyActExpeditionModule) moduleMap.get(MConst.FamilyActExpe);
//        FamilyAuth auth = familyModule.getAuth();
//        if (auth == null || auth.getFamilyId() <= 0 || auth.getFamilyLevel() < reqFamilyLevel) {
//            familyModule.warn("family_tips_transnobuild", Integer.toString(reqFamilyLevel));
//            return false;
//        }
//        if (expeditionModule.getInt(F_MAX_ID, 0) < reqMaxPassedExpeId) {
//            FamilyExpeditionVo expeVo = FamilyActExpeditionManager.expeditionVoMap.get(reqMaxPassedExpeId).get(1);
//            expeditionModule.warn("family_tips_transnoexp", expeVo.getName());
//            return false;
//        }
        return true;
    }
}
