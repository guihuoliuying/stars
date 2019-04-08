package com.stars.modules.familyactivities.bonfire.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.bonfire.FamilyBonfirePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2016/10/8.
 */
public class ClientFamilyScene extends PlayerPacket {
    private int sceneId;
    private String postionStr;

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public void setPostionStr(String postionStr) {
        this.postionStr = postionStr;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
            buff.writeInt(sceneId);
            buff.writeString(postionStr);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyBonfirePacketSet.C_FAMILY_SCENE;
    }
}
