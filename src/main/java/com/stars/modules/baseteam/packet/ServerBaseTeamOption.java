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
public class ServerBaseTeamOption extends PlayerPacket {
    private byte tag;// 子协议

    private byte teamType;// 队伍类型
    private int teamTarget;// 队伍目标
    private long targetRoleId;// 目标roleId
    private byte openApply;// 开放申请标志

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", BaseTeamPacketSet.Server_TeamOption));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        BaseTeamModule teamModule = module(MConst.Team);
        switch (tag) {
            case 0:// 创建队伍
                teamModule.createTeam(teamType, teamTarget);
                break;
            case 1:// 踢人
                teamModule.kickOutTeam(targetRoleId);
                break;
//            case 2:// 解散队伍------已废弃
//                teamModule.disband();
//                break;
            case 3:// 变更队长
                teamModule.changeCaptain(targetRoleId);
                break;
            case 4:// 退出队伍
                teamModule.leaveTeam();
                break;
            case 5:// 设置自动加入队伍标志
                teamModule.setOpenApplyFlag(openApply);
                break;
            case 6:// 强行改变队伍目标(不需要提示队员是否满足条件)
                teamModule.changeTeamTarget(teamTarget , true);
                break;
            case 7:// 改变队伍目标(要提示队员是否满足条件)
                teamModule.changeTeamTarget(teamTarget , false);
                break;
            case 8:// 组队副本中强制回城
                teamModule.backToCity();
                break;
        }
    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Server_TeamOption;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        tag = buff.readByte();
        switch (tag) {
            case 0:// 创建队伍
                this.teamType = buff.readByte();
                this.teamTarget = buff.readInt();
                break;
            case 1:// 踢人
                targetRoleId = Long.parseLong(buff.readString());
                break;
            case 3:// 变更队长
                targetRoleId = Long.parseLong(buff.readString());
                break;
            case 5:// 设置自动加入队伍标志
                openApply = buff.readByte();
                break;
            case 6:// 改变队伍目标
                this.teamTarget = buff.readInt();
                break;
            case 7:// 改变队伍目标
                this.teamTarget = buff.readInt();
                break;
        }
    }
}
