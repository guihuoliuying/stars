package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;

/**
 * Created by liuyuheng on 2016/8/10.
 */
public class ServerPauseTime extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.pauseFightTime();
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_PAUSE_TIME;
    }
}
