package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-04-17 18:36
 */
public class ClientFamilyWarBattleStat extends PlayerPacket {
    private int myKillCount;
    private int myDeadCount;
    private int myAssistCount;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(myKillCount);
        buff.writeInt(myDeadCount);
        buff.writeInt(myAssistCount);
        LogUtil.info("familywar|roleid:{},杀人数:{},死亡数:{},助攻数:{}", getRoleId(), myKillCount, myDeadCount, myAssistCount);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_STAT;
    }

    public void addMyKillCount(int myKillCount) {
        this.myKillCount += myKillCount;
    }

    public void addMyDeadCount(int myDeadCount) {
        this.myDeadCount += myDeadCount;
    }

    public void addMyAssistCount(int myAssistCount) {
        this.myAssistCount += myAssistCount;
    }

    public void setMyKillCount(int myKillCount) {
        this.myKillCount = myKillCount;
    }

    public void setMyDeadCount(int myDeadCount) {
        this.myDeadCount = myDeadCount;
    }

    public void setMyAssistCount(int myAssistCount) {
        this.myAssistCount = myAssistCount;
    }
}
