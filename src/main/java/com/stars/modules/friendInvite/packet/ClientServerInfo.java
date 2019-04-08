package com.stars.modules.friendInvite.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friendInvite.InvitePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/8.
 */
public class ClientServerInfo extends PlayerPacket {

    /**
     * 邀请方服务器ID
     */
    private int serverId;

    /**
     * 邀请方服务器名称
     */
    private String serverName;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(serverId);
        buff.writeString(serverName);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return InvitePacketSet.C_SERVER_INFO;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
