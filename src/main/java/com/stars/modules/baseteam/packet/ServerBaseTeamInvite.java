package com.stars.modules.baseteam.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/11/15.
 */
public class ServerBaseTeamInvite extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    /* 参数 */
    private byte teamType;// 队伍类型
    private String[] invitees;// 邀请的roleId
    private long invitor;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", BaseTeamPacketSet.Server_TeamInvite));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        BaseTeamModule teamModule = module(MConst.Team);
        switch (reqType) {
            case 0:// 可邀请成员列表
                teamModule.canInviteList(teamType);
                break;
            case 1:// 请求收到的邀请列表
                teamModule.reqReceiveInvite(teamType);
                break;
            case 2:// 邀请入队
                for (String string : invitees) {
                    teamModule.inviteJoinTeam(Long.parseLong(string));
                }
                break;
            case 3:// 同意邀请
                teamModule.permitInvite(teamType, invitor);
                break;
            case 4:// 一键清空所有邀请
                teamModule.clearAllInvite(teamType);
                break;
        }
    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Server_TeamInvite;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 0:// 可邀请成员列表
                this.teamType = buff.readByte();
                break;
            case 1:// 请求收到的邀请列表
                this.teamType = buff.readByte();
                break;
            case 2:// 邀请入队
                byte size = buff.readByte();
                invitees = new String[size];
                for (int i = 0; i < size; i++) {
                    invitees[i] = buff.readString();
                }
                break;
            case 3:// 同意邀请
                this.teamType = buff.readByte();
                invitor = Long.parseLong(buff.readString());
                break;
            case 4:// 一键清空所有邀请
                this.teamType = buff.readByte();
                break;
        }
    }
}
