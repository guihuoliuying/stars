package com.stars.modules.familyactivities.invade.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.invade.FamilyInvadePacket;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.activities.invade.cache.AwardBoxCache;
import com.stars.services.family.activities.invade.cache.InvadeDamageCache;
import com.stars.services.family.activities.invade.cache.MonsterNpcCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/22.
 */
public class ClientFamilyInvade extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SPAWN_MONSTER_NPC = 1;// npc列表/刷触发npc
    public static final byte UPDATE_MONSTER_NPC = 2;// 触发npc状态更新
    public static final byte SPAWN_AWARD_BOX = 3;// 刷宝箱
    public static final byte UPDATE_AWARD_BOX = 4;// 宝箱状态更新
    public static final byte RANK_LIST = 5;// 伤害排行榜更新
    public static final byte BACK_TO_CITY = 6;// 回城消息

    private Collection<MonsterNpcCache> monsterNpcCacheList;// 怪物npc
    private Map<Integer, Byte> npcStatusChange;
    private Map<String, AwardBoxCache> awardBoxMap;// 宝箱
    private Map<String, Boolean> awardBoxStatus;// 宝箱状态
    private String awardBoxUId;// 宝箱Id
    private List<InvadeDamageCache> damageRankList;// 伤害排行榜

    public ClientFamilyInvade() {
    }

    public ClientFamilyInvade(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyInvadePacket.C_INVADE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SPAWN_MONSTER_NPC:
                writeMonsterNpc(buff);
                break;
            case UPDATE_MONSTER_NPC:
                writeNpcStatusChange(buff);
                break;
            case SPAWN_AWARD_BOX:
                writeAwardBox(buff);
                break;
            case UPDATE_AWARD_BOX:
                buff.writeString(awardBoxUId);
                buff.writeByte((byte) (awardBoxStatus.get(awardBoxUId) ? 1 : 0));
                break;
            case RANK_LIST:
                writeRankList(buff);
                break;
        }
    }

    private void writeMonsterNpc(NewByteBuffer buff) {
        byte size = (byte) (monsterNpcCacheList == null ? 0 : monsterNpcCacheList.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (MonsterNpcCache cache : monsterNpcCacheList) {
            cache.writeToBuff(buff);
        }
    }

    private void writeAwardBox(NewByteBuffer buff) {
        byte size = (byte) (awardBoxMap == null ? 0 : awardBoxMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (AwardBoxCache boxCache : awardBoxMap.values()) {
            boxCache.writeToBuff(buff);
            buff.writeByte((byte) (awardBoxStatus.get(boxCache.getAwardBoxUId()) ? 1 : 0));
        }
    }

    private void writeRankList(NewByteBuffer buff) {
        byte size = (byte) (damageRankList == null ? 0 : damageRankList.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (InvadeDamageCache damageCache : damageRankList) {
            damageCache.writeToBuff(buff);
        }
    }

    private void writeNpcStatusChange(NewByteBuffer buff) {
        byte size = (byte) (npcStatusChange == null ? 0 : npcStatusChange.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Integer, Byte> entry : npcStatusChange.entrySet()) {
            buff.writeInt(entry.getKey());// npcId
            buff.writeByte(entry.getValue());// 状态
        }
    }

    public void setMonsterNpcCacheList(Collection<MonsterNpcCache> monsterNpcCacheList) {
        this.monsterNpcCacheList = monsterNpcCacheList;
    }

    public void setMonsterNpcStatus(int monsterNpcUId, byte monsterNpcStatus) {
        npcStatusChange = new HashMap<>();
        npcStatusChange.put(monsterNpcUId, monsterNpcStatus);
    }

    public void setAwardBoxMap(Map<String, AwardBoxCache> awardBoxMap) {
        this.awardBoxMap = awardBoxMap;
    }

    public void setAwardBoxStatus(Map<String, Boolean> awardBoxStatus) {
        this.awardBoxStatus = awardBoxStatus;
    }

    public void setAwardBoxUId(String awardBoxUId) {
        this.awardBoxUId = awardBoxUId;
    }

    public void setDamageRankList(List<InvadeDamageCache> damageRankList) {
        this.damageRankList = damageRankList;
    }

    public void setMonsterNpcStatus(Map<Integer, Byte> npcStatusChange) {
        this.npcStatusChange = npcStatusChange;
    }
}
