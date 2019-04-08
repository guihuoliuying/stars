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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/12/24.
 */
public class ServerTPGScoreRank extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private List<Integer> teamIdList = new LinkedList<>();

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", TeamPVPGamePacketSet.S_TPG_SCORERANK));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        TeamPVPGameModule tpgModule = module(MConst.TeamPVPGame);
        switch (reqType) {
            case 1:// 请求积分赛排行榜
                ServiceHelper.tpgLocalService().reqScoreRank(getRoleId());
                break;
            case 2:// 根据teamid获得排名
                ServiceHelper.tpgLocalService().reqScoreRanking(getRoleId(), teamIdList);
                break;
        }
    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.S_TPG_SCORERANK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 2:
                byte size = buff.readByte();
                if (size <= 0)
                    return;
                for (int i = 0; i < size; i++) {
                    teamIdList.add(buff.readInt());
                }
                break;
        }
    }
}
