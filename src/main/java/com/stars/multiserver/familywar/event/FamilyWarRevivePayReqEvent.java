package com.stars.multiserver.familywar.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/11/28.
 */
public class FamilyWarRevivePayReqEvent extends Event {

    private int fightServerId;
    private String battleId;
    private String fightId;
    private String figterUid;
    
    public FamilyWarRevivePayReqEvent() {
    	
    }
    
    public FamilyWarRevivePayReqEvent(String battleId, String fightId, String fighterUid) {
    	this.battleId = battleId;
    	this.fightId = fightId;
    	this.figterUid = fighterUid;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public String getFigterUid() {
        return figterUid;
    }

    public void setFigterUid(String figterUid) {
        this.figterUid = figterUid;
    }

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}
}
