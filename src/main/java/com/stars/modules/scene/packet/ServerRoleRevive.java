package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/10/8.
 */
public class ServerRoleRevive extends PlayerPacket {
    private byte subType;
    public static final byte role = 1;//角色
    public static final byte budy = 2;//伙伴
    private String buddyId;//伙伴id,此处是场景中伙伴的uid
    private byte stageType;//场景类型

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        switch (subType) {
            case role: {
                LogUtil.info("复活相关|roleId:{},stageType:{}", getRoleId(), stageType);
                sceneModule.revive(stageType);
            }
            break;
            case budy: {
                sceneModule.reviveBuddy(stageType, buddyId);
            }
            break;
        }
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_REVIVE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.subType = buff.readByte();
        this.stageType = buff.readByte();
        switch (subType) {
            case budy: {
                this.buddyId = buff.readString();
            }
            break;
        }
    }
}
