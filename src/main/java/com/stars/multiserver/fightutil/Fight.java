package com.stars.multiserver.fightutil;

import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.fightbase.FightBaseService;
import com.stars.util.MapUtil;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Long.parseLong;

//import com.stars.multiserver.MainRpcHelper;

/**
 * Created by chenkeyu on 2017-05-04 18:07
 */
public class Fight {
    private String fightId;
    private int fightServerId;
    private long creationTimestamp;
    private int camp1MainServerId;
    private int camp2MainServerId;
    private long camp1Id;
    private long camp2Id;
    private long camp1TotalFightScore;
    private long camp2TotalFightScore;
    private int camp1BuffId;
    private int camp2BuffId;
    private Set<Integer> camp1BuffInstanceId;
    private Set<Integer> camp2BuffInstanceId;
    private Map<String, FighterEntity> camp1FighterMap;
    private Map<String, FighterEntity> camp2FighterMap;
    private Map<String, FighterEntity> fighterMap = new HashMap<>();
    private Map<String, Map<String, Long>> sufferedDamageMap = new HashMap<>(); // (受害者, (攻击者, 伤害值))
    private Map<String, Integer> comboKillCountMap = new HashMap<>(); // (roleId, 连杀数)
    private int invincibleBuffInstId;
    private ConcurrentHashMap<String, Long> reviveMap;
    private FightStat stat;
    private AbstractBattle battle;
    /* 内存数据，方便调用 */
    private Map<Long, List<Long>> fighterIdListMap; // (campId, list of fighter id)
    private FightBaseService fightBaseService;

    public void initFight(AbstractBattle battle, FightStat stat) {
        this.battle = battle;
        this.stat = stat;
        fighterIdListMap = new HashMap<>();
        List<Long> list = null;
        list = new ArrayList<>();
        for (String roleId : camp1FighterMap.keySet()) {
            list.add(parseLong(roleId));
        }
        fighterIdListMap.put(camp1Id, list);
        list = new ArrayList<>();
        for (String roleId : camp2FighterMap.keySet()) {
            list.add(parseLong(roleId));
        }
        fighterIdListMap.put(camp2Id, list);
//        args.setCamp1MainServerId(camp1MainServerId);
//        args.setCamp2MainServerId(camp2MainServerId);
        Map<Long, Byte> campMap = new HashMap<>();
        for (String roleId : camp1FighterMap.keySet()) {
            campMap.put(parseLong(roleId), FightConst.CAMP1);
        }
        for (String roleId : camp2FighterMap.keySet()) {
            campMap.put(parseLong(roleId), FightConst.CAMP2);
        }
        for (FighterEntity entity : fighterMap.values()) {
            long fighterId = parseLong(entity.getUniqueId());
            stat.addPersonalStat(fighterId, entity.getName(), campMap.get(fighterId));
        }
        reviveMap = new ConcurrentHashMap<>();
        camp1BuffInstanceId = new HashSet<>();
        camp2BuffInstanceId = new HashSet<>();
    }

    public void startFight(short fightConst, byte[] packet, Object args) {
        fightBaseService.createFight(fightServerId, fightConst,
                MultiServerHelper.getServerId(), fightId, packet, args);
    }

    public void addMonster(short fightConst, Map<String, FighterEntity> nonPlayerEntity) {
        fightBaseService.addMonster(fightServerId, com.stars.multiserver.fight.handler.FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, new ArrayList<>(nonPlayerEntity.values()));
    }

    public void enterFight(int mainServerId, long campId, long roleId, int stageId, short fightConst) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        byte camp = campId == camp1Id ? FightConst.CAMP1 : FightConst.CAMP2;
        String fighterId = Long.toString(roleId);
        FighterEntity myEntity = fighterMap.get(fighterId);
        if (myEntity == null) return;
        myEntity.setState((byte) 0);//每次进入都设置没复活过
        myEntity.setCamp(camp);
        if (camp == FightConst.CAMP1) {
            myEntity.setPosition(stageVo.getPosition());
            myEntity.setRotation(stageVo.getRotation());
        } else {
            myEntity.setPosition(stageVo.getEnemyPos(0));
            myEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(myEntity);
        fightBaseService.addFighter(
                fightServerId,
                fightConst,
                mainServerId,
                fightId,
                list);
    }

    public boolean revive(String fighterUid) {
        return reviveMap.contains(fighterUid);
    }

    public void handleDamage(Map<String, HashMap<String, Integer>> damageMap) {
        for (Map.Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
            String victimId = entry.getKey();
            Map<String, Integer> victimSufferedDamageMap = entry.getValue();
            if (camp1FighterMap.containsKey(victimId) || camp2FighterMap.containsKey(victimId)) {
                if (!sufferedDamageMap.containsKey(victimId)) {
                    sufferedDamageMap.put(victimId, toStringLongMap(victimSufferedDamageMap));
                } else {
                    MapUtil.add(sufferedDamageMap.get(victimId), toStringLongMap(victimSufferedDamageMap));
                }
            }
            battle.handleDamage(victimId, victimSufferedDamageMap);
        }
    }

    public void handleDead(Map<String, String> deadMap) {
        for (Map.Entry<String, String> dead : deadMap.entrySet()) {
            String victimUid = dead.getKey();
            String attackerUid = dead.getValue();
            battle.handleDead(victimUid, attackerUid);
        }
    }

    public synchronized void handleRevive(String fighterUid, short fightConst) {
        if (!reviveMap.containsKey(fighterUid)) return;
        FighterEntity entity = fighterMap.get(fighterUid);
        if (entity == null) return;
        List<FighterEntity> entityList = new ArrayList<>();
        ArrayList<String> reviveList = new ArrayList<>();
        entityList.add(entity);
        reviveList.add(entity.uniqueId);
        fightBaseService.addFighter(fightServerId, fightConst,
                MultiServerHelper.getServerId(), fightId, entityList);
        //发送满血满状态指令
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_RESET_CHARACS);
        serverOrder.setUniqueIDs(reviveList);
        sendServerOrder(fightId, serverOrder, fightConst);//发送服务端lua命令
    }

    public void setBuff(long campId, byte selfTarget, byte otherTarget, int buffId, int debuffId, int buffLevel, short fightConst) {
        if (campId == camp1Id) {
            removeBuffOrder(camp1BuffInstanceId, FightConst.CAMP1, fightConst);
            removeBuffOrder(camp2BuffInstanceId, FightConst.CAMP2, fightConst);
            ServerOrder order = ServerOrders.newAddBuffOrder(FightConst.CAMP1, selfTarget, buffId, buffLevel);
            camp1BuffId = buffId;
            camp1BuffInstanceId.add(order.getInstanceId());
            fightBaseService.addServerOrder(fightServerId, fightConst,
                    MultiServerHelper.getServerId(), fightId, order);
            ServerOrder order2 = ServerOrders.newAddBuffOrder(FightConst.CAMP2, otherTarget, debuffId, buffLevel);
            camp2BuffInstanceId.add(order2.getInstanceId());
            fightBaseService.addServerOrder(fightServerId, fightConst,
                    MultiServerHelper.getServerId(), fightId, order2);
        } else if (campId == camp2Id) {
            removeBuffOrder(camp1BuffInstanceId, FightConst.CAMP1, fightConst);
            removeBuffOrder(camp2BuffInstanceId, FightConst.CAMP2, fightConst);
            ServerOrder order = ServerOrders.newAddBuffOrder(FightConst.CAMP2, selfTarget, buffId, buffLevel);
            camp2BuffId = buffId;
            camp2BuffInstanceId.add(order.getInstanceId());
            fightBaseService.addServerOrder(fightServerId, fightConst,
                    MultiServerHelper.getServerId(), fightId, order);
            ServerOrder order2 = ServerOrders.newAddBuffOrder(FightConst.CAMP1, otherTarget, debuffId, buffLevel);
            camp1BuffInstanceId.add(order2.getInstanceId());
            fightBaseService.addServerOrder(fightServerId, fightConst,
                    MultiServerHelper.getServerId(), fightId, order2);
        }
    }

    public FightStat updateCampPoints(long campId, long points) {
        return stat.updateCampPoints(campId, points);
    }

    public FightStat getStat() {
        return stat;
    }

    public FightStat updateMorale(long campId, int moraleDelta) {
        return stat.updateCampMorale(campId, moraleDelta);
    }

    public void stopFight(short fightConst) {
        fightBaseService.stopFight(
                fightServerId,
                fightConst,
                MultiServerHelper.getServerId(),
                fightId);
    }

    public FightResult endFight() {
        FightResult result = new FightResult();
        if (stat.getCamp1TotalPoints() > stat.getCamp2TotalPoints()) {
            setResult(result, camp1Id, camp2Id);
        } else if (stat.getCamp1TotalPoints() < stat.getCamp2TotalPoints()) {
            setResult(result, camp2Id, camp1Id);
        } else {
            if (stat.getCamp1Morale() > stat.getCamp2Morale()) {
                setResult(result, camp1Id, camp2Id);
            } else if (stat.getCamp1Morale() < stat.getCamp2Morale()) {
                setResult(result, camp2Id, camp1Id);
            } else {
                if (camp1TotalFightScore > camp2TotalFightScore) {
                    setResult(result, camp1Id, camp2Id);
                } else if (camp1TotalFightScore < camp2TotalFightScore) {
                    setResult(result, camp2Id, camp1Id);
                } else {
                    if (camp1Id > camp2Id) {
                        setResult(result, camp1Id, camp2Id);
                    } else {
                        setResult(result, camp2Id, camp1Id);
                    }
                }
            }
        }
        result.setStat(stat);
        return result;
    }

//    public int getMainServerId(String fighterUid) {
//        if (camp1FighterMap.containsKey(fighterUid)) {
//            return camp1MainServerId;
//        }
//        if (camp2FighterMap.containsKey(fighterUid)) {
//            return camp2MainServerId;
//        }
//        return 0;
//    }

    public long getCampId(String fighterUid) {
        if (camp1FighterMap.containsKey(fighterUid)) {
            return camp1Id;
        }
        if (camp2FighterMap.containsKey(fighterUid)) {
            return camp2Id;
        }
        return 0;
    }

    public byte getCamp(String fighterUid){
        if (camp1FighterMap.containsKey(fighterUid)) {
            return FightConst.CAMP1;
        }else {
            return FightConst.CAMP2;
        }
    }

    public long getOpponentCampId(long campId) {
        if (campId == camp1Id) {
            return camp2Id;
        }
        return camp1Id;
    }

    private void setResult(FightResult result, long winnerId, long loserId) {
        result.setWinnerCampId(winnerId);
        result.setLoserCampId(loserId);
    }

    private Map<String, Long> toStringLongMap(Map<String, Integer> map) {
        Map<String, Long> newMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().longValue());
        }
        return newMap;
    }

    /**
     * 发送服务端lua命令
     */
    public void sendServerOrder(String fightId, ServerOrder serverOrder, short fightConst) {
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addOrder(serverOrder);
        byte[] bytes = packetToBytes(packet);
        fightBaseService.addServerOrder(fightServerId, fightConst,
                MultiServerHelper.getServerId(), fightId, bytes);
    }

    /**
     * 将包转为byte[]
     */
    private byte[] packetToBytes(Packet packet) {
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        packet.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    private void removeBuffOrder(Set<Integer> buffInstanceId, byte camp, short fightConst) {
        if (!buffInstanceId.isEmpty()) {
            for (int buffInstId : buffInstanceId) {
                fightBaseService.addServerOrder(fightServerId, fightConst,
                        MultiServerHelper.getServerId(), fightId,
                        ServerOrders.newRemoveBuffOrder(camp, buffInstId));
            }
        }
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public int getCamp1MainServerId() {
        return camp1MainServerId;
    }

    public void setCamp1MainServerId(int camp1MainServerId) {
        this.camp1MainServerId = camp1MainServerId;
    }

    public int getCamp2MainServerId() {
        return camp2MainServerId;
    }

    public void setCamp2MainServerId(int camp2MainServerId) {
        this.camp2MainServerId = camp2MainServerId;
    }

    public long getCamp1Id() {
        return camp1Id;
    }

    public void setCamp1Id(long camp1Id) {
        this.camp1Id = camp1Id;
    }

    public long getCamp2Id() {
        return camp2Id;
    }

    public void setCamp2Id(long camp2Id) {
        this.camp2Id = camp2Id;
    }

    public long getCamp1TotalFightScore() {
        return camp1TotalFightScore;
    }

    public void setCamp1TotalFightScore(long camp1TotalFightScore) {
        this.camp1TotalFightScore = camp1TotalFightScore;
    }

    public long getCamp2TotalFightScore() {
        return camp2TotalFightScore;
    }

    public void setCamp2TotalFightScore(long camp2TotalFightScore) {
        this.camp2TotalFightScore = camp2TotalFightScore;
    }

    public int getCamp1BuffId() {
        return camp1BuffId;
    }

    public void setCamp1BuffId(int camp1BuffId) {
        this.camp1BuffId = camp1BuffId;
    }

    public int getCamp2BuffId() {
        return camp2BuffId;
    }

    public void setCamp2BuffId(int camp2BuffId) {
        this.camp2BuffId = camp2BuffId;
    }

    public Set<Integer> getCamp1BuffInstanceId() {
        return camp1BuffInstanceId;
    }

    public void setCamp1BuffInstanceId(Set<Integer> camp1BuffInstanceId) {
        this.camp1BuffInstanceId = camp1BuffInstanceId;
    }

    public Set<Integer> getCamp2BuffInstanceId() {
        return camp2BuffInstanceId;
    }

    public void setCamp2BuffInstanceId(Set<Integer> camp2BuffInstanceId) {
        this.camp2BuffInstanceId = camp2BuffInstanceId;
    }

    public Map<String, FighterEntity> getCamp1FighterMap() {
        return camp1FighterMap;
    }

    public void setCamp1FighterMap(Map<String, FighterEntity> camp1FighterMap) {
        this.camp1FighterMap = camp1FighterMap;
        this.fighterMap.putAll(camp1FighterMap);
    }

    public Map<String, FighterEntity> getCamp2FighterMap() {
        return camp2FighterMap;
    }

    public void setCamp2FighterMap(Map<String, FighterEntity> camp2FighterMap) {
        this.camp2FighterMap = camp2FighterMap;
        this.fighterMap.putAll(camp2FighterMap);
    }

    public Map<String, FighterEntity> getFighterMap() {
        return fighterMap;
    }

    public void setFighterMap(Map<String, FighterEntity> fighterMap) {
        this.fighterMap = fighterMap;
    }

    public Map<String, Map<String, Long>> getSufferedDamageMap() {
        return sufferedDamageMap;
    }

    public void setSufferedDamageMap(Map<String, Map<String, Long>> sufferedDamageMap) {
        this.sufferedDamageMap = sufferedDamageMap;
    }

    public Map<String, Integer> getComboKillCountMap() {
        return comboKillCountMap;
    }

    public void setComboKillCountMap(Map<String, Integer> comboKillCountMap) {
        this.comboKillCountMap = comboKillCountMap;
    }

    public int getInvincibleBuffInstId() {
        return invincibleBuffInstId;
    }

    public void setInvincibleBuffInstId(int invincibleBuffInstId) {
        this.invincibleBuffInstId = invincibleBuffInstId;
    }

    public ConcurrentHashMap<String, Long> getReviveMap() {
        return reviveMap;
    }

    public void setReviveMap(ConcurrentHashMap<String, Long> reviveMap) {
        this.reviveMap = reviveMap;
    }

    public AbstractBattle getBattle() {
        return battle;
    }

    public Map<Long, List<Long>> getFighterIdListMap() {
        return fighterIdListMap;
    }

    public void setFighterIdListMap(Map<Long, List<Long>> fighterIdListMap) {
        this.fighterIdListMap = fighterIdListMap;
    }

	public FightBaseService getFightBaseService() {
		return fightBaseService;
	}

	public void setFightBaseService(FightBaseService fightBaseService) {
		this.fightBaseService = fightBaseService;
	}
}
