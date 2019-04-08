package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ServerFamilyWarBattleFightDirect extends PlayerPacket {

    private byte towerCamp;
    private byte towerType;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_BATTLE_FIGHT_DIRECT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        towerCamp = buff.readByte();
        towerType = buff.readByte();
    }

    public byte getTowerCamp() {
        return towerCamp;
    }

    public byte getTowerType() {
        return towerType;
    }
}
