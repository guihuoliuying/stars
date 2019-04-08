package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * 通知客户端添加/移除player;
 * Created by panzhenfeng on 2016/10/22.
 */
public class ClientLootTreasureAddRemovePlayer  extends PlayerPacket {
    private List<FighterEntity> newFighterList = null;
    private ClientUpdatePlayer clientUpdatePlayer = new ClientUpdatePlayer();

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        clientUpdatePlayer.writeToBuffer(buff);
        byte size = (byte)(newFighterList==null?0:newFighterList.size());
        buff.writeByte(size);
        for(byte i = 0; i<size; i++){
            buff.writeString(newFighterList.get((int)i).getUniqueId());
        }
    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_ADDREMOVE_PLAYER;
    }

    public void setNewFighter(List<FighterEntity> newFighter) {
        newFighterList = newFighter;
        clientUpdatePlayer.setNewFighter(newFighter);
    }

    public void setRemoveFighter(List<String> removeFighter) {
        clientUpdatePlayer.setRemoveFighter(removeFighter);
    }
}
