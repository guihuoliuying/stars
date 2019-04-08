package com.stars.modules.friendShare.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friendShare.SharePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/8.
 */
public class ClientShare extends PlayerPacket {

    /**
     * 领奖状态
     */
    private byte status;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 下载地址
     */
    private String link;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(status);
        buff.writeString(inviteCode);
        buff.writeString(link);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SharePacketSet.C_SHARE;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
