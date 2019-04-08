package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightUpdateInfo extends PlayerPacket {

    private int camp1Morale;
    private int camp1BuffId;
    private int camp1BuffLevel;
    private long camp1Points;
    private double camp1TowerHp;//百分比
    private int camp2Morale;
    private int camp2BuffId;
    private int camp2BuffLevel;
    private long camp2Points;
    private double camp2TowerHp;//百分比
    private Map<String, EliteFightTower> towerMap;

    private byte subType;

    public ClientFamilyWarBattleFightUpdateInfo() {
    }

    public ClientFamilyWarBattleFightUpdateInfo(byte subType, int camp1Morale, int camp2Morale, int camp1BuffId, int camp2BuffId,
                                                long camp1Points, long camp2Points, double camp1TowerHp, double camp2TowerHp, Map<String, EliteFightTower> towerMap) {
        this.subType = subType;
        this.camp1Morale = camp1Morale;
        this.camp2Morale = camp2Morale;
        this.camp1BuffId = camp1BuffId;
        this.camp2BuffId = camp2BuffId;
        this.camp1Points = camp1Points;
        this.camp2Points = camp2Points;
        this.camp1TowerHp = camp1TowerHp;
        this.camp2TowerHp = camp2TowerHp;
        this.towerMap = towerMap;
    }

    public ClientFamilyWarBattleFightUpdateInfo(byte subType, double camp1TowerHp, double camp2TowerHp, Map<String, EliteFightTower> towerMap) {
        this.camp1TowerHp = camp1TowerHp;
        this.camp2TowerHp = camp2TowerHp;
        this.towerMap = towerMap;
        this.subType = subType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_UPDATE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        buff.writeByte(subType);
        if (subType == ClientFamilyWarBattleFightInitInfo.ELITE) {
            buff.writeInt(camp1Morale); // 阵营1的士气
            buff.writeInt(camp1BuffId);//阵营1buffid
            buff.writeInt(1);
            buff.writeString(Long.toString(camp1Points));//阵营1总积分
            buff.writeInt(camp2Morale); // 阵营2的士气
            buff.writeInt(camp2BuffId);
            buff.writeInt(1);
            buff.writeString(Long.toString(camp2Points));//阵营2总积分
        } else {
            buff.writeInt(0); // 阵营1的士气
            buff.writeInt(0);//阵营1buffid
            buff.writeInt(1);
            buff.writeString(Long.toString(0));//阵营1总积分
            buff.writeInt(0); // 阵营2的士气
            buff.writeInt(0);
            buff.writeInt(1);
            buff.writeString(Long.toString(camp2Points));//阵营2总积分
        }
        buff.writeString(decimalFormat.format(camp1TowerHp));//阵营1的塔血量百分比
        buff.writeString(decimalFormat.format(camp2TowerHp));//阵营2的塔血量百分比
        buff.writeInt(towerMap.size()); // 大小
        LogUtil.info("familywar|subType:{},camp1Morale:{},camp1BuffId:{},camp1Points:{},camp1TowerHp:{},camp2Morale:{},camp2BuffId:{},camp2Points:{},camp2TowerHp:{}",
                subType, camp1Morale, camp1BuffId, camp1Points, camp1TowerHp, camp2Morale, camp2BuffId, camp2Points, camp2TowerHp);
        for (EliteFightTower tower : towerMap.values()) {
            buff.writeString(tower.getUid()); // 塔的唯一id
            buff.writeInt(tower.getHp()); // 塔的血量
            LogUtil.info("tower|update|subType:{},uid:{},hp:{}", subType, tower.getUid(), tower.getHp());
        }

    }

    public long getCamp1Points() {
        return camp1Points;
    }

    public void setCamp1Points(long camp1Points) {
        this.camp1Points = camp1Points;
    }

    public long getCamp2Points() {
        return camp2Points;
    }

    public void setCamp2Points(long camp2Points) {
        this.camp2Points = camp2Points;
    }
}
