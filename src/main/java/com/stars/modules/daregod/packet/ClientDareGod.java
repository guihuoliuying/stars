package com.stars.modules.daregod.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daregod.DareGodManager;
import com.stars.modules.daregod.DareGodPacketSet;
import com.stars.modules.daregod.prodata.SsbBoss;
import com.stars.modules.daregod.prodata.SsbBossTarget;
import com.stars.modules.daregod.prodata.SsbRankAward;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daregod.userdata.RankRoleDareGodCache;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class ClientDareGod extends PlayerPacket {
    public static final byte VIEW = 0x00;//打开界面响应
    public static final byte VIEW_RANK = 0x01;//打开排行榜响应
    public static final byte UPDATE_TIMES = 0x02;//更新购买次数
    public static final byte UPDATE_DAMAGE_MAP = 0x03;//更新领取的目标奖励
    public static final byte AFTER_ENTER_FIGHT = 0x04;//进入战斗场景后的数据

    private byte subType;
    private List<SsbBoss> ssbBossList = new ArrayList<>();
    private Map<Integer, List<SsbRankAward>> rankAwardMap = new HashMap<>();

    private Map<Integer, LinkedList<RankRoleDareGodCache>> dareGodCacheMap = new HashMap<>();//
    private Map<Integer, Integer> targetDropMap = new HashMap<>();//可领取 or 未领取 or 不可领取的伤害奖励 long,1 or 0 or -1|...;
    private Map<Integer, Integer> targetDropGroupMap = new HashMap<>();//
    private int canFightTimes;
    private int canBuyTimes;
    private int reqItems;
    private int roleFightType;
    private long totalDamage;

    private int myRank;
    private LinkedList<RankRoleDareGodCache> dareGodCaches = new LinkedList<>();

    public ClientDareGod() {
    }

    public ClientDareGod(byte subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case VIEW:
                buff.writeByte((byte) ssbBossList.size());
                for (SsbBoss ssbBoss : ssbBossList) {
                    ssbBoss.writeToBuff(buff);
                }
                buff.writeByte((byte) dareGodCacheMap.size());
                for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : dareGodCacheMap.entrySet()) {
                    buff.writeInt(entry.getKey());//不同战力分段
                    buff.writeByte((byte) entry.getValue().size());
                    for (RankRoleDareGodCache dareGodCache : entry.getValue()) {
                        dareGodCache.writeToBuffer(buff);
                    }
                }
                buff.writeInt(roleFightType);//玩家所处的阶段
                buff.writeInt(canFightTimes);//还剩多少次
                buff.writeInt(canBuyTimes);
                buff.writeInt(reqItems);
                buff.writeLong(totalDamage);
                buff.writeByte((byte) targetDropMap.size());
                for (Map.Entry<Integer, Integer> entry : targetDropMap.entrySet()) {
                    buff.writeInt(entry.getKey());//伤害Id
                    SsbBossTarget bossTarget = DareGodManager.ssbBossTargetMap.get(entry.getKey());
                    buff.writeLong(bossTarget.getTargetHurt());
                    buff.writeInt(entry.getValue());//可领取 or 未领取 or 不可领取 | 1 or 0 or -1
                    buff.writeInt(targetDropGroupMap.get(entry.getKey()));//奖励groupId
                }
                break;
            case VIEW_RANK:
                buff.writeByte((byte) dareGodCacheMap.size());
                for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : dareGodCacheMap.entrySet()) {
                    buff.writeInt(entry.getKey());//不同战力分段
                    buff.writeString(DareGodManager.getFightTypeDesc(entry.getKey()));
                    buff.writeString(DareGodManager.getFightTypeShape(entry.getKey()));
                    com.stars.util.LogUtil.info("subServer:{},fightType:{}", getSubServer(MultiServerHelper.getServerId()), entry.getKey());
                    SsbBoss ssbBoss = DareGodManager.getSsbBossByType(getSubServer(MultiServerHelper.getServerId()), entry.getKey());
                    buff.writeInt(ssbBoss.getFightingMax());
                    buff.writeInt(ssbBoss.getFightingMin());
                    buff.writeByte((byte) entry.getValue().size());
                    for (RankRoleDareGodCache dareGodCache : entry.getValue()) {
                        dareGodCache.writeToBuffer(buff);
                    }
                }
                break;
            case UPDATE_TIMES:
                buff.writeInt(canFightTimes);
                buff.writeInt(canBuyTimes);
                break;
            case UPDATE_DAMAGE_MAP:
                buff.writeByte((byte) targetDropMap.size());
                for (Map.Entry<Integer, Integer> entry : targetDropMap.entrySet()) {
                    buff.writeInt(entry.getKey());//伤害值
                    SsbBossTarget bossTarget = DareGodManager.ssbBossTargetMap.get(entry.getKey());
                    buff.writeLong(bossTarget.getTargetHurt());
                    buff.writeInt(entry.getValue());//可领取 or 未领取 or 不可领取 | 1 or 0 or -1
                    buff.writeInt(targetDropGroupMap.get(entry.getKey()));//奖励groupId
                }
                break;
            case AFTER_ENTER_FIGHT:
                buff.writeInt(myRank);
                buff.writeLong(totalDamage);
                buff.writeByte((byte) dareGodCaches.size());
                for (RankRoleDareGodCache dareGodCach : dareGodCaches) {
                    dareGodCach.writeToBuff(buff);
                }
                LogUtil.info("进入包|挑战女神");
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return DareGodPacketSet.C_DAREGOD;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public void setTargetDropMap(Map<Integer, Integer> targetDropMap) {
        this.targetDropMap = targetDropMap;
    }

    public void setCanFightTimes(int canFightTimes) {
        this.canFightTimes = canFightTimes;
    }

    public void setCanBuyTimes(int canBuyTimes) {
        this.canBuyTimes = canBuyTimes;
    }

    public void setReqItems(int reqItems) {
        this.reqItems = reqItems;
    }

    public void setSsbBossList(List<SsbBoss> ssbBossList) {
        this.ssbBossList = ssbBossList;
    }

    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }

    public void setDareGodCaches(LinkedList<RankRoleDareGodCache> dareGodCaches) {
        this.dareGodCaches = dareGodCaches;
    }

    public void setDareGodCacheMap(Map<Integer, LinkedList<RankRoleDareGodCache>> dareGodCacheMap) {
        this.dareGodCacheMap = dareGodCacheMap;
    }

    public void setRoleFightType(int roleFightType) {
        this.roleFightType = roleFightType;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }

    public void setTargetDropGroupMap(Map<Integer, Integer> targetDropGroupMap) {
        this.targetDropGroupMap = targetDropGroupMap;
    }

    private int getSubServer(int mainServerId) {
        return mainServerId / 1000;
    }
}
