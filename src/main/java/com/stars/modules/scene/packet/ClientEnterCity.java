package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/7/19.
 */
public class ClientEnterCity extends PlayerPacket {
    private String[] transferTarget;// 传送目标信息

    public ClientEnterCity() {
    }

    public ClientEnterCity(int citySceneId, String pos) {
        transferTarget = new String[]{String.valueOf(citySceneId), pos};
    }

    public ClientEnterCity(String[] transferTarget) {
        this.transferTarget = transferTarget;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_CITY_TRANSFER;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(Integer.parseInt(transferTarget[0]));
        buff.writeString(transferTarget[1]);
    }
}
