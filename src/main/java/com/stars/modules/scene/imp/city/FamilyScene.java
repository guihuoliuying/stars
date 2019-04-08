package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;
import com.stars.modules.familyactivities.bonfire.packet.ClientFamilyScene;
import com.stars.modules.familyactivities.invade.FamilyInvadeModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.util.MapUtil;

import java.util.Map;

/**
 *
 * 家族领地场景
 * Created by zhouyaohui on 2016/10/8.
 */
public class FamilyScene extends ArroundScene {

    private String position;

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        FamilyBonfireModule fm = (FamilyBonfireModule) moduleMap.get(MConst.FamilyActBonfire);
        if (fm.hasFamily()) {
            return true;
        }
        return false;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        FamilyBonfireModule fm = (FamilyBonfireModule) moduleMap.get(MConst.FamilyActBonfire);
        fm.clear();
        int safeId = MapUtil.getInt(DataManager.commonConfigMap, "family_gohome", 0);
        SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(safeId);
        ClientFamilyScene packet = new ClientFamilyScene();
        packet.setSceneId(curSafeinfoVo.getSafeId());
        if (roleModule.getSafeStageId() != safeId) {
            /** 上一次不是家族场景，初始化出生位置 */
            position = curSafeinfoVo.getCharPosition();
            packet.setPostionStr(curSafeinfoVo.getCharPosition());
        } else {
            position = roleModule.getRoleRow().getPositionStr();
            packet.setPostionStr(roleModule.getRoleRow().getPositionStr());
        }
        // 先发剧情播放记录
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.sendPlayedDrama(this, getSceneId());
        roleModule.send(packet);
       // 更新家族入侵活动数据
        FamilyInvadeModule familyInvadeModule = (FamilyInvadeModule) moduleMap.get(MConst.FamilyActInvade);
        familyInvadeModule.updateMember();

        FamilyBonfireModule familyBonfireModule = (FamilyBonfireModule) moduleMap.get(MConst.FamilyActBonfire);
        familyBonfireModule.updateMember();
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        // 更新家族入侵活动数据
        FamilyInvadeModule familyInvadeModule = (FamilyInvadeModule) moduleMap.get(MConst.FamilyActInvade);
        familyInvadeModule.removeMemeber();
        FamilyBonfireModule familyBonfireModule = (FamilyBonfireModule) moduleMap.get(MConst.FamilyActBonfire);
        familyBonfireModule.removeMemeber();
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        return String.valueOf(familyModule.getAuth().getFamilyId());
    }
}
