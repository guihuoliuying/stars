package com.stars.modules.role.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/1.
 */
public class ServerFightScoreReward extends PlayerPacket {
    private int rewardId;

    @Override
    public void execPacket(Player player) {
        /*RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.rewardFightScore(rewardId);*/
    }

    @Override
    public short getType() {
        return RolePacketSet.S_FIGHTSCORE_REWARD;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.rewardId = buff.readInt();
    }
}
