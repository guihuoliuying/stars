package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightInitInfo extends PlayerPacket {
    public static final byte ELITE = 0x00;
    public static final byte NORMAL = 0x01;

    private Map<String, EliteFightTower> towerMap;
    private byte battleType;
    private byte subBattleType;

    private byte subType;

    public ClientFamilyWarBattleFightInitInfo() {
    }

    public ClientFamilyWarBattleFightInitInfo(byte subType, Map<String, EliteFightTower> towerMap) {
        this.subType = subType;
        this.towerMap = towerMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_INIT_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        buff.writeInt(towerMap.size()); // 大小
        for (EliteFightTower tower : towerMap.values()) {
            buff.writeString(tower.getUid()); // 唯一id
            buff.writeByte(tower.getCamp()); // 阵营
            buff.writeByte(tower.getType()); // 类型
            buff.writeInt(tower.getMaxHp()); // 最大血量
            buff.writeString(tower.getPos()); // 位置
//            LogUtil.info("tower|uid:{},camp:{},type:{},maxHp:{},hp:{},pos:{}", tower.getUid(), tower.getCamp(), tower.getType(), tower.getMaxHp(), tower.getHp(), tower.getPos());
        }
        if (subType == ELITE) {
            buff.writeInt(FamilyWarConst.battleType);//1:本服 2:跨服海选 3:跨服决赛
            buff.writeByte(battleType);//本服的情况下(1:八强赛，2:四强赛，3:决赛，4:季殿赛)|
            // 跨服海选(1,2,3,4,5分别代表第X天的战斗)|跨服决赛(32,16,8,4,2,1分别代表x强赛)
            buff.writeByte(subBattleType);
        }
        LogUtil.info("familywar|塔的初始信息 warType:{},battleType:{},sub:{}", FamilyWarConst.battleType, battleType, subBattleType);
    }

    public void setBattleType(byte battleType) {
        this.battleType = battleType;
    }

    public void setSubBattleType(byte subBattleType) {
        this.subBattleType = subBattleType;
    }
}
