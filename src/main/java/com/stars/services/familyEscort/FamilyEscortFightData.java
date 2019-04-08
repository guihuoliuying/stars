package com.stars.services.familyEscort;

/**
 * Created by zhaowenshuo on 2017/4/19.
 */
public class FamilyEscortFightData {

    private String fightId; // 战斗id
    private int fightServerId; // 战斗服id
    private long attackerId; // 攻击者（劫镖者）
    private long defenderId; // 防御者（运镖者）
    private long attackerFamilyId; // 攻击者的家族id
    private long defenderFamilyId; // 防御者的家族id
    private long enterFamilyId;//当前场景的家族id
    

    public FamilyEscortFightData() {
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

    public long getAttackerId() {
        return attackerId;
    }

    public void setAttackerId(long attackerId) {
        this.attackerId = attackerId;
    }

    public long getDefenderId() {
        return defenderId;
    }

    public void setDefenderId(long defenderId) {
        this.defenderId = defenderId;
    }

    public long getAttackerFamilyId() {
        return attackerFamilyId;
    }

    public void setAttackerFamilyId(long attackerFamilyId) {
        this.attackerFamilyId = attackerFamilyId;
    }

    public long getDefenderFamilyId() {
        return defenderFamilyId;
    }

    public void setDefenderFamilyId(long defenderFamilyId) {
        this.defenderFamilyId = defenderFamilyId;
    }

	public long getEnterFamilyId() {
		return enterFamilyId;
	}

	public void setEnterFamilyId(long enterFamilyId) {
		this.enterFamilyId = enterFamilyId;
	}

}
