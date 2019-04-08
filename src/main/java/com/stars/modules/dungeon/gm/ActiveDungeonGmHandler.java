package com.stars.modules.dungeon.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.userdata.RoleChapter;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/16.
 */
public class ActiveDungeonGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
            for (DungeoninfoVo dungeoninfoVo : DungeonManager.dungeonVoMap.values()) {
                if (dungeonModule.isDungeonActive(dungeoninfoVo.getDungeonId())) continue;
                RoleDungeon roleDungeon = new RoleDungeon(roleId, dungeoninfoVo.getDungeonId());
                dungeonModule.putRoleDungeonMap(roleDungeon);
                dungeonModule.context().insert(roleDungeon);
                if (!dungeonModule.isChapterActive(dungeoninfoVo.getWorldId())) {
                    RoleChapter roleChapter = new RoleChapter(roleId, dungeoninfoVo.getWorldId());
                    dungeonModule.putRoleChapterMap(roleChapter);
                    dungeonModule.context().insert(roleChapter);
                }
            }
            PlayerUtil.send(roleId, new ClientText("执行成功, activedungeonall"));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, activedungeonall"));
        }
    }
}
