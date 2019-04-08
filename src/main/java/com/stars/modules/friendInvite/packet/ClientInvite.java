package com.stars.modules.friendInvite.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friendInvite.InvitePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/8.
 */
public class ClientInvite extends PlayerPacket {

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 下载地址
     */
    private String link;

    /**
     * 累计邀请好友个数
     */
    private int inviteCount;

    /**
     * 累计领取奖励次数
     */
    private int fetchCount;


    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(inviteCode);
        buff.writeString(link);
        buff.writeInt(inviteCount);
        buff.writeInt(fetchCount);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return InvitePacketSet.C_INVATE;
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

    public int getInviteCount() {
        return inviteCount;
    }

    public void setInviteCount(int inviteCount) {
        this.inviteCount = inviteCount;
    }

    public int getFetchCount() {
        return fetchCount;
    }

    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }
}
