package com.stars.modules.friendInvite.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friendInvite.InvitePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/8.
 */
public class ClientBeInvite extends PlayerPacket {

    /**
     * 绑定的邀请码
     */
    private String bindInviteCode;

    /**
     * 领奖状态
     */
    private byte status;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(bindInviteCode);
        buff.writeByte(status);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return InvitePacketSet.C_BE_INVATE;
    }

    public String getBindInviteCode() {
        return bindInviteCode;
    }

    public void setBindInviteCode(String bindInviteCode) {
        this.bindInviteCode = bindInviteCode;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }
}
