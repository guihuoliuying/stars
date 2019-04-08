package com.stars.modules.scene.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/2/28.
 */
public class EnterStageGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            int dungeonId = Integer.parseInt(args[0]);
            DungeoninfoVo dungeoninfoVo = DungeonManager.getDungeonVo(dungeonId);
            if(dungeoninfoVo == null){
                PlayerUtil.send(roleId, new ClientText("执行失败, enterstage"));
            }
            DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
            dungeonModule.sendAllChapterData();

            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            sceneModule.setGmDungeonId(dungeonId);
            sceneModule.enterScene(SceneManager.SCENETYPE_DUNGEON,dungeonId,dungeonId);
            PlayerUtil.send(roleId, new ClientText("执行成功, enterstage"));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, enterstage"));
        }
    }
}
