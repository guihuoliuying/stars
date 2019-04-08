package com.stars.modules.teampvpgame.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.teampvpgame.TeamPVPGameModule;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class ServerTPGData extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", TeamPVPGamePacketSet.S_TPG_DATA));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        TeamPVPGameModule tpgModule = module(MConst.TeamPVPGame);
        switch (reqType) {
            case 1:// 打开界面请求
                tpgModule.updateTPGTeamMember();
                ServiceHelper.tpgLocalService().reqCurTPGData(getRoleId());
                break;
            case 2:// 请求积分赛匹配结果
                ServiceHelper.tpgLocalService().reqScoreMatchResult(getRoleId());
                break;
        }

    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.S_TPG_DATA;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {

        }
    }
}
