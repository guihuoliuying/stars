package com.stars.modules.fightingmaster.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.fightingmaster.FightingMasterPacketSet;
import com.stars.modules.fightingmaster.event.FightReadyEvent;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2016/11/9.
 */
public class ServerFightReady extends PlayerPacket {

    private int sceneType;


    @Override
    public short getType() {
        return FightingMasterPacketSet.S_FIGHT_READY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        sceneType = buff.readInt();
    }

    @Override
    public void execPacket(Player player) {
        eventDispatcher().fire(new FightReadyEvent(sceneType));
    }

}
