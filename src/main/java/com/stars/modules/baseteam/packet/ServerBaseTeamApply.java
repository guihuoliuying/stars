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
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/11/15.
 */
public class ServerBaseTeamApply extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    /* 参数 */
    private int teamId;
    private long applyRoleId;// 申请者Id
    private byte teamType;// 队伍类型
    private int teamTarget;// 队伍目标

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", BaseTeamPacketSet.Server_TeamApply));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        BaseTeamModule teamModule = module(MConst.Team);
        switch (reqType) {
            case 0:// 申请列表
                ServiceHelper.baseTeamService().reqApplyList(getRoleId());
                break;
            case 1:// 申请入队
                teamModule.applyJoinTeam(teamId, teamTarget, teamType);
                break;
            case 2:// 同意申请入队
                teamModule.permitApply(applyRoleId);
                break;
            case 3:// 拒绝申请入队
                teamModule.refuseApply(applyRoleId);
                break;
            case 4:// 清空申请列表
                teamModule.reqClearApplyList();
                break;
            case 5:// 直接加入队伍（从聊天信息那里进入）
                teamModule.joinTeamByChat(teamId, teamTarget);
                break;
            case 6:// 可申请队伍列表
                teamModule.canApplyTeam(teamType);
                break;
            case 7:// 关闭可申请队伍列表界面处理
                teamModule.handleCloseTeamUI(teamType);
                break;
            case 8:// 打开可申请队伍列表界面处理
                teamModule.handleOpenTeamUI(teamType);
                break;
        }

    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Server_TeamApply;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 申请入队
                this.teamId = buff.readInt();
                this.teamTarget = buff.readInt();
                this.teamType = buff.readByte();
                break;
            case 2:// 同意申请入队
                this.applyRoleId = Long.parseLong(buff.readString());
                break;
            case 3:// 拒绝申请入队
                this.applyRoleId = Long.parseLong(buff.readString());
                break;
            case 5:// 直接加入队伍（从聊天信息那里进入）
                this.teamId = buff.readInt();
                this.teamTarget = buff.readInt();
                break;
            case 6:// 可申请队伍列表
                this.teamType = buff.readByte();
                break;
            case 7:// 关闭可申请队伍列表界面
                this.teamType = buff.readByte();
                break;
            case 8:// 打开可申请队伍列表界面
                this.teamType = buff.readByte();
                break;
            default:
                break;
        }
    }
}
