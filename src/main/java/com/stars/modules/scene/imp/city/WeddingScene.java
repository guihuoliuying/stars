package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.marry.packet.ClientMarry;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.services.marry.userdata.MarryWedding;
import com.stars.util.MapUtil;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/12.
 */
public class WeddingScene extends ArroundScene {

    private String position;

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        return roleModule.getLastEnterWeddingSceneId();
    }

    @Override
    public String getPosition() {
        return null;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        String key = roleModule.getLastEnterWeddingSceneId() + "";
        MarryWedding luxurious = ServiceHelper.marryService().getWeddingSync(key);
        if (luxurious != null && luxurious.getState() == MarryWedding.RUN) {
            return true;
        }else {
            roleModule.setLastEnterWeddingSceneId("");
            return false;
        }
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int safeId = MapUtil.getInt(DataManager.commonConfigMap, "marry_party_stageid", 0);
        SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(safeId);
        ClientMarry packet = new ClientMarry();
        packet.setResType(ClientMarry.ENTER_WEDDING);
        packet.setSceneId(curSafeinfoVo.getSafeId());
        if (roleModule.getSafeStageId() != safeId) {
            position = curSafeinfoVo.getCharPosition();
            packet.setPosition(curSafeinfoVo.getCharPosition());
        } else {
            position = roleModule.getRoleRow().getPositionStr();
            packet.setPosition(roleModule.getRoleRow().getPositionStr());
        }
        packet.setMarryKey(roleModule.getLastEnterWeddingSceneId());
        roleModule.send(packet);
        ServiceHelper.marryService().enterWeddingScene(roleModule.getLastEnterWeddingSceneId(),roleModule.id(), true);  // 登陆进入婚礼场景，需要调用这个方法
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        ServiceHelper.marryService().exitWeddingScene(roleModule.getLastEnterWeddingSceneId(),roleModule.id());
        roleModule.setLastEnterWeddingSceneId("");
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
