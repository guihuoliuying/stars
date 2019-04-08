package com.stars.modules.familyactivities.war.packet.fight;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-04-14 11:33
 */
public class ClientFamilyWarBattleResult extends PlayerPacket {
    private byte result;//胜利与否1:胜利,0:失败
    private String myFamilyName;
    private byte myEliteWinCount;
    private int myMatchWinCount;
    private int myKillCount;
    private long myFamilyPoints;
    private String myServerName;
    private String enemyFamilyName;
    private byte enemyEliteWinCount;
    private int enemyMatchWinCount;
    private int enemyKillCount;
    private long enemyFamilyPoints;
    private int battleType;
    private long nextBattleResultTime;
    private String enemyServerName;
    private int warType;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(result);
        buff.writeString(myFamilyName);
        buff.writeByte(myEliteWinCount);
        buff.writeInt(myMatchWinCount);
        buff.writeInt(myKillCount);
        buff.writeLong(myFamilyPoints);
        buff.writeString(myServerName);
        buff.writeString(enemyFamilyName);
        buff.writeByte(enemyEliteWinCount);
        buff.writeInt(enemyMatchWinCount);
        buff.writeInt(enemyKillCount);
        buff.writeLong(enemyFamilyPoints);
        buff.writeInt(battleType);
        buff.writeInt(warType);
        buff.writeLong(nextBattleResultTime);
        buff.writeString(enemyServerName);
        LogUtil.info("familywar|roleId:{}战斗类型:{}的结果:{}==[我方家族名称:{},我方精英战场胜利次数:{},我方匹配战场胜利次数:{},我方击杀:{},我方总积分:{}]==" +
                        "[对方家族名称:{},对方精英战场胜利次数:{},对方匹配战场胜利次数:{},对方击杀:{},对方总积分:{}]",
                getRoleId(), battleType, result, myFamilyName, myEliteWinCount, myMatchWinCount, myKillCount, myFamilyPoints,
                enemyFamilyName, enemyEliteWinCount, enemyMatchWinCount, enemyKillCount, enemyFamilyPoints);
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_RESULT;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public void setMyFamilyName(String myFamilyName) {
        this.myFamilyName = myFamilyName;
    }

    public void setMyEliteWinCount(byte myEliteWinCount) {
        this.myEliteWinCount = myEliteWinCount;
    }

    public void setMyKillCount(int myKillCount) {
        this.myKillCount = myKillCount;
    }

    public void setMyFamilyPoints(long myFamilyPoints) {
        this.myFamilyPoints = myFamilyPoints;
    }

    public void setEnemyFamilyName(String enemyFamilyName) {
        this.enemyFamilyName = enemyFamilyName;
    }

    public void setEnemyEliteWinCount(byte enemyEliteWinCount) {
        this.enemyEliteWinCount = enemyEliteWinCount;
    }

    public void setMyMatchWinCount(int myMatchWinCount) {
        this.myMatchWinCount = myMatchWinCount;
    }

    public void setEnemyMatchWinCount(int enemyMatchWinCount) {
        this.enemyMatchWinCount = enemyMatchWinCount;
    }

    public void setEnemyKillCount(int enemyKillCount) {
        this.enemyKillCount = enemyKillCount;
    }

    public void setEnemyFamilyPoints(long enemyFamilyPoints) {
        this.enemyFamilyPoints = enemyFamilyPoints;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public void setNextBattleResultTime(long nextBattleResultTime) {
        this.nextBattleResultTime = nextBattleResultTime;
    }

    public void setMyServerName(String myServerName) {
        this.myServerName = myServerName;
    }

    public void setEnemyServerName(String enemyServerName) {
        this.enemyServerName = enemyServerName;
    }

    public void setWarType(int warType) {
        this.warType = warType;
    }
}
