package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientEnterCity;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/19.
 */
public class SafeCityScene extends ArroundScene {

    private String position;

	public SafeCityScene(){
		
	}
	
    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        return true;
    }

    /** 进入安全区，对类型进行修正，策划希望回城可以到上一次退出的安全区 */
//	public byte checkNewSceneType(byte newSceneType, Object extend, Map<String, Module> moduleMap) {
//		RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
//		SafeinfoVo safeinfoVo = SceneManager.getSafeVo(roleModule.getSafeStageId());
//		byte sceneType = (byte) safeinfoVo.getType();
//		return sceneType;
//	}
    
    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        String transferPos = (String) obj.toString();// 传送坐标
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        int curSafeId = roleModule.getSafeStageId();
        SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(curSafeId);
        if (curSafeinfoVo == null) {
            /** 找不到安全区产品数据，初始化 */
            roleModule.updateSafeStageId(SceneManager.initSafeStageId);
            curSafeinfoVo = SceneManager.getSafeVo(roleModule.getSafeStageId());
            roleModule.getRoleRow().setPositionStr(curSafeinfoVo.getCharPosition());
            sceneModule.getScene().setSceneId(SceneManager.initSafeStageId);
        }
        ClientEnterCity packet;
        if (StringUtil.isEmpty(transferPos)) {// 坐标为空,进入当前所在城镇场景
            int[] p = roleModule.getRoleRow().getPosition();
            if (p[0] == 0 && p[1] == 0 && p[2] == 0) {
                /** 用出生点初始化 */
                String[] strp = curSafeinfoVo.getCharPosition().split("[+]");
                p[0] = Integer.valueOf(strp[0]);
                p[1] = Integer.valueOf(strp[1]);
                p[2] = Integer.valueOf(strp[2]);
            }
            packet = new ClientEnterCity(roleModule.getSafeStageId(), roleModule.getRoleRow().getPositionStr());
            position = roleModule.getRoleRow().getPositionStr();
        } else {// 坐标不为空,根据坐标传送到目标城镇场景
            String[] targetInfo = curSafeinfoVo.getTransTargetInfo(transferPos);
            // 更新玩家所在安全区Id
            roleModule.updateSafeStageId(Integer.parseInt(targetInfo[0]));
            packet = new ClientEnterCity(Integer.parseInt(targetInfo[0]), targetInfo[1]);
            position = targetInfo[1];
        }
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, getSceneId());
        sceneModule.send(packet);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        return "";
    }
}
