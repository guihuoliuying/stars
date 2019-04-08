package com.stars.modules.role.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/2.
 */
public class ClientFighScoreReward extends PlayerPacket {
    private int rewardId;
    private byte isSuc;// 1=成功;0=失败

    public ClientFighScoreReward() {
    }

    public ClientFighScoreReward(int rewardId, byte isSuc) {
        this.rewardId = rewardId;
        this.isSuc = isSuc;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RolePacketSet.C_FIGHTSCORE_REWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(rewardId);
        buff.writeByte(isSuc);
    }
}
