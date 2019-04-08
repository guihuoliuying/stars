package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightDirect extends PlayerPacket {

    private String directorName;
    private byte towerCamp;
    private byte towerType;

    public ClientFamilyWarBattleFightDirect() {
    }

    public ClientFamilyWarBattleFightDirect(String directorName, byte towerCamp, byte towerType) {
        this.directorName = directorName;
        this.towerCamp = towerCamp;
        this.towerType = towerType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_DIRECT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(directorName);
        buff.writeByte(towerCamp);
        buff.writeByte(towerType);
    }
}
