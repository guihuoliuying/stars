package com.stars.modules.familyactivities.war.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/12/30.
 */
public class FamilyWarFighterAddingSucceededEvent extends Event {

    private int fightServerId;
    private byte sceneType;

    public FamilyWarFighterAddingSucceededEvent(int fightServerId, byte sceneType) {
        this.fightServerId = fightServerId;
        this.sceneType = sceneType;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }

	public byte getSceneType() {
		return sceneType;
	}

	public void setSceneType(byte sceneType) {
		this.sceneType = sceneType;
	}
}
