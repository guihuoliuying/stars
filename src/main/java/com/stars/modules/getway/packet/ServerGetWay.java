package com.stars.modules.getway.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.getway.GetWayModule;
import com.stars.modules.getway.GetWayPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class ServerGetWay extends PlayerPacket {

    private List<Integer> getWayIdList;

    @Override
    public short getType() {
        return GetWayPacketSet.S_GETWAY;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int size = buff.readInt();
        getWayIdList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            getWayIdList.add(buff.readInt());
        }
    }

    @Override
    public void execPacket(Player player) {
        GetWayModule module = module(MConst.GetWay);
        module.view(getWayIdList);
    }

}
