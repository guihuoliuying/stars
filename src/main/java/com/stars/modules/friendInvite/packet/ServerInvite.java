package com.stars.modules.friendInvite.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friendInvite.InviteModule;
import com.stars.modules.friendInvite.InvitePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/8.
 */
public class ServerInvite extends PlayerPacket {

    public static final byte REQ_VIEW = 0x01;       // 请求打开界面
    public static final byte REQ_BIND = 0x02;       // 绑定邀请码
    public static final byte REQ_AWARD = 0x03;      // 领取邀请奖励
    public static final byte REQ_AWARD_BE = 0x04;   // 领取受邀请奖励

    public byte subtype;

    private String inviteCode;

    @Override
    public void execPacket(Player player) {
        InviteModule inviteModule = module(MConst.FriendInvite);
        switch (subtype) {
            case REQ_VIEW:
                inviteModule.view();
                break;
            case REQ_BIND:
                inviteModule.bindInviteCode(inviteCode);
                break;
            case REQ_AWARD:
                inviteModule.award();
                break;
            case REQ_AWARD_BE:
                inviteModule.awardBe();
                break;
        }
    }

    @Override
    public short getType() {
        return InvitePacketSet.S_INVATE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW:
                break;
            case REQ_BIND:
                inviteCode = buff.readString();
                break;
        }
    }

}
