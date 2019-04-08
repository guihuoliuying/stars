package com.stars.modules.getway.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.getway.GetWayPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class ClientGetWay extends PlayerPacket {

    private List<Integer> getWayIdList;

    public ClientGetWay() {
    }

    public ClientGetWay(List<Integer> getWayIdList) {
        this.getWayIdList = getWayIdList;
    }

    @Override
    public short getType() {
        return GetWayPacketSet.C_GETWAY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        if (getWayIdList == null) {
            buff.writeInt(0);
        } else {
            buff.writeInt(getWayIdList.size());
            for (int getWayId : getWayIdList) {
                buff.writeInt(getWayId);
            }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

}
