package com.stars.multiserver.fightutil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-04 18:54
 */
public abstract class AbstractBattle {
    protected String battleId;
    protected Fight fight;

    public abstract void onInitFight();

    public abstract void enterFight(int mainServerId, long campId, long roleId);

    public abstract void handleDead(String victimUid, String attackerUid);

    public abstract void handleDamage(String victimUid, Map<String, Integer> victimSufferedDamageMap);

    public abstract void handleRevive(String fighterUid);

    public abstract void stopFight();

    public abstract FightResult endFight();

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }
}
