package com.stars.modules.dungeon.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/5/18.
 */
public class DungeonPassAllGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
            for (DungeoninfoVo dungeoninfoVo : DungeonManager.dungeonVoMap.values()) {
                if (!dungeonModule.isDungeonActive(dungeoninfoVo.getDungeonId())) {
                    continue;
                }
                dungeonModule.passDungeon(dungeoninfoVo.getDungeonId(), (byte) 3);

            }
            PlayerUtil.send(roleId, new ClientText("执行dungeon.passAll成功"));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行dungeon.passAll失败"));
        }
    }
}
