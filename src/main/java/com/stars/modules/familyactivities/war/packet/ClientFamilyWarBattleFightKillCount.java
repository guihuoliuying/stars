package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightKillCount extends PlayerPacket {

    private int killCount;

    public ClientFamilyWarBattleFightKillCount() {
    }

    public ClientFamilyWarBattleFightKillCount(int killCount) {
        this.killCount = killCount;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_KILL_COUNT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(killCount);
    }
}
