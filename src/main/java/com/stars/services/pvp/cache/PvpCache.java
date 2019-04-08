package com.stars.services.pvp.cache;

import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuyuheng on 2016/11/2.
 */
public class PvpCache {
    private long invitor;// 邀请者id
    private long invitee;// 受邀者id
    private long startTimestamp;// 开始时间戳
    private Map<String, FighterEntity> entityMap;
    private byte status;// 状态,使用SceneManager中战斗副本状态
    private int fightActorId;// 战斗服actorId
    private String fightId;
    private int fightServer;

//    public PvpCache(long invitor, long invitee, Collection<FighterEntity> entityList, int fightActorId) {
//        this.invitor = invitor;
//        this.invitee = invitee;
//        this.entityMap = new HashMap<>();
//        for (FighterEntity entity : entityList) {
//            entityMap.put(entity.getUniqueId(), entity);
//        }
//        this.startTimestamp = System.currentTimeMillis();
//        this.status = SceneManager.STAGE_PROCEEDING;
//        this.fightActorId = fightActorId;
//    }

    public PvpCache(long invitor, long invitee, Collection<FighterEntity> entityList, String fightId) {
        this.invitor = invitor;
        this.invitee = invitee;
        this.entityMap = new HashMap<>();
        for (FighterEntity entity : entityList) {
            entityMap.put(entity.getUniqueId(), entity);
        }
        this.startTimestamp = System.currentTimeMillis();
        this.status = SceneManager.STAGE_PROCEEDING;
        this.fightId = fightId;
    }

    /**
     * 胜负判断
     *
     * @return [胜利者roleId,失败者roleId]
     */
    public long[] judgeVictory() throws Exception {
        this.status = SceneManager.STAGE_VICTORY;
        String loser;
        String winner;
        FighterEntity selfPlayerEntity = null;
        FighterEntity enemyPlayerEntity = null;
        for (FighterEntity entity : entityMap.values()) {
            if (entity.getFighterType() != FighterEntity.TYPE_PLAYER)
                continue;
            if (entity.getCamp() == FighterEntity.CAMP_SELF) {
                selfPlayerEntity = entity;
            }
            if (entity.getCamp() == FighterEntity.CAMP_ENEMY) {
                enemyPlayerEntity = entity;
            }
        }
        // 剩余血量
        double selfRest = selfPlayerEntity.getAttribute().getHp() / selfPlayerEntity.getAttribute().getMaxhp() * 1.0;
        double enemyRest = enemyPlayerEntity.getAttribute().getHp() / enemyPlayerEntity.getAttribute().getMaxhp() * 1.0;
        if (selfRest != enemyRest) {
            if (selfRest > enemyRest) {
                winner = selfPlayerEntity.getUniqueId();
                loser = enemyPlayerEntity.getUniqueId();
            } else {
                winner = enemyPlayerEntity.getUniqueId();
                loser = selfPlayerEntity.getUniqueId();
            }
            return new long[]{Long.parseLong(winner), Long.parseLong(loser)};
        }
        // 战力
        if (selfPlayerEntity.getFightScore() != enemyPlayerEntity.getFightScore()) {
            if (selfPlayerEntity.getFightScore() > enemyPlayerEntity.getFightScore()) {
                winner = selfPlayerEntity.getUniqueId();
                loser = enemyPlayerEntity.getUniqueId();
            } else {
                winner = enemyPlayerEntity.getUniqueId();
                loser = selfPlayerEntity.getUniqueId();
            }
            return new long[]{Long.parseLong(winner), Long.parseLong(loser)};
        }
        // 随机
        int random = new Random().nextInt(2);
        if (random > 0) {
            winner = selfPlayerEntity.getUniqueId();
            loser = enemyPlayerEntity.getUniqueId();
        } else {
            winner = enemyPlayerEntity.getUniqueId();
            loser = selfPlayerEntity.getUniqueId();
        }
        return new long[]{Long.parseLong(winner), Long.parseLong(loser)};
    }

    public boolean isPlayer(String entityId) {
        return entityMap.get(entityId).getFighterType() == FighterEntity.TYPE_PLAYER;
    }

    public void updateHp(String entityId, int value) {
        entityMap.get(entityId).changeHp(value * -1);
    }

    public long getInvitor() {
        return invitor;
    }

    public long getInvitee() {
        return invitee;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public int getFightActorId() {
        return fightActorId;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

	public int getFightServer() {
		return fightServer;
	}

	public void setFightServer(int fightServer) {
		this.fightServer = fightServer;
	}
}
