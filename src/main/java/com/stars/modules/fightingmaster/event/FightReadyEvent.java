package com.stars.modules.fightingmaster.event;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2016/11/16.
 */
public class FightReadyEvent extends Event {

    private int sceneType;

    public FightReadyEvent(int sceneType) {
        this.sceneType = sceneType;
    }

    public int getSceneType() {
        return sceneType;
    }
}
