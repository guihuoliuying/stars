package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-04-17 18:38
 */
public class ClientFamilyWarBattleFamilyPoints extends PlayerPacket {
    private long myFamilyPoints;
    private long enemyFamilyPoints;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeLong(myFamilyPoints);
        buff.writeLong(enemyFamilyPoints);
        LogUtil.info("familywar|roleid:{},更新双方家族积分数据|我方家族积分:{},敌方家族积分:{}", getRoleId(), myFamilyPoints, enemyFamilyPoints);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FAMILY_POINTS;
    }

    public void setMyFamilyPoints(long myFamilyPoints) {
        this.myFamilyPoints = myFamilyPoints;
    }

    public void addMyFamilyPoints(long myFamilyPoints) {
        this.myFamilyPoints += myFamilyPoints;
    }

    public void addEnemyFamilyPoints(long enemyFamilyPoints) {
        this.enemyFamilyPoints += enemyFamilyPoints;
    }

    public void setEnemyFamilyPoints(long enemyFamilyPoints) {
        this.enemyFamilyPoints = enemyFamilyPoints;
    }
}
