package com.stars.modules.dungeon.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/15.
 */
public class ResetCountGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
            dungeonModule.resetAllEnterCount();
            dungeonModule.warn("执行成功, resetdungeoncount");
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, resetdungeoncount"));
        }
    }
}
