package com.stars.modules.baseteam.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/11/16.
 */
public class ClientBaseTeamMatch extends PlayerPacket {
    private byte tag;

    public static final byte CANCEL_MATCH_TEAM = 0;// 取消匹配队伍
    public static final byte CANCEL_MATCH_MEMBER = 1;// 取消匹配队员

    public ClientBaseTeamMatch() {
    }

    public ClientBaseTeamMatch(byte tag) {
        this.tag = tag;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Client_TeamMatch;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(tag);
    }
}
