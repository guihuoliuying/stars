package com.stars.modules.rank.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.rank.RankPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.userdata.AbstractRankPo;

import java.util.List;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class ClientRank extends PlayerPacket {
    private int rankId;

    private List<AbstractRankPo> list;

    public ClientRank() {
    }

    public ClientRank(int rankId) {
        this.rankId = rankId;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RankPacketSet.C_RANK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(rankId);
        short size = (short) (list == null ? 0 : list.size());
        buff.writeShort(size);
        for (AbstractRankPo roleRankPo : list) {
            roleRankPo.writeToBuffer(rankId, buff);
        }
    }

    public void setList(List<AbstractRankPo> list) {
        this.list = list;
    }
}
