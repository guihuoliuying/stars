package com.stars.modules.scene.packet.fightSync;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 通知客户端更改对应role的战斗属性;(目前用于更改阵营)
 * Created by panzhenfeng on 2016/10/19.
 */
public class ClientChangeFightVoAttr extends PlayerPacket {

    private String roleId;
    private String attrName;
    private int value;

    public ClientChangeFightVoAttr() {

    }

    public ClientChangeFightVoAttr(String roleId, String attrName, int value){
        this.roleId = roleId;
        this.attrName = attrName;
        this.value = value;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_CHANGEATTR;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(this.roleId);
        buff.writeString(this.attrName);
        buff.writeInt(this.value);
    }
}
