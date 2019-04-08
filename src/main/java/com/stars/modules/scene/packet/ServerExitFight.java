package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ServerExitFight extends PlayerPacket {

    public ServerExitFight() {
    }

    public ServerExitFight(long roleId) {
        setRoleId(roleId);
    }

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.backToCity();

    }

    @Override
    public short getType() {
        return ScenePacketSet.S_EXITFIGHT;
    }
}
