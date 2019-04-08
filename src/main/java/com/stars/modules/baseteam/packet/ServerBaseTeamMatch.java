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
 * Created by liuyuheng on 2016/11/16.
 */
public class ServerBaseTeamMatch extends PlayerPacket {
    private byte tag;

    /* 参数 */
    private byte teamType;// 队伍类型
    private int teamTarget;// 队伍目标(默认为0)

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", BaseTeamPacketSet.Server_TeamMatch));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        BaseTeamModule teamModule = module(MConst.Team);
        switch (tag) {
            case 0:// 请求匹配队伍
                teamModule.reqMatchTeam(teamType, teamTarget);
                break;
            case 1:// 取消匹配队伍
                teamModule.reqCancelMatchTeam(true);
                break;
            case 2:// 请求匹配队员
                teamModule.reqMatchMember();
                break;
            case 3:// 取消匹配队员
                teamModule.reqCancelMatchMember(null);
                break;
        }
    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Server_TeamMatch;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.tag = buff.readByte();
        switch (tag) {
            case 0:// 请求匹配队伍
                this.teamType = buff.readByte();
                this.teamTarget = buff.readInt();
                break;
        }
    }
}
