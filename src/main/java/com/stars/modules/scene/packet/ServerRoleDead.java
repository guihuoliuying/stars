package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/7/11.
 */
public class ServerRoleDead extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        LogUtil.info("收到角色死亡包");
        sceneModule.roleDead();
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_ROLEDEAD;
    }
}
