package com.stars.modules.newserverfightscore.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.newserverfightscore.NewServerFightModule;
import com.stars.modules.newserverfightscore.NewServerFightPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2017/1/7.
 */
public class ServerNSFightScore extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private int rewardId;// 奖励Id

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", NewServerFightPacketSet.S_NSFIGHTSCORE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        NewServerFightModule nsfModule = module(MConst.NewServerFightScore);
        switch (reqType) {
            case 1:// 请求数据
                nsfModule.reqData();
                break;
            case 2:// 领取奖励
                nsfModule.takeReward(rewardId);
                break;
            case 3:// 历史排行榜
                ServiceHelper.newServerFightScoreService().sendHistoryRank(getRoleId());
                break;
        }
    }

    @Override
    public short getType() {
        return NewServerFightPacketSet.S_NSFIGHTSCORE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 2:// 领取奖励
                this.rewardId = buff.readInt();
                break;
        }
    }
}
