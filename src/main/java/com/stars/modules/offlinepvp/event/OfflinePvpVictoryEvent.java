package com.stars.modules.offlinepvp.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class OfflinePvpVictoryEvent extends Event {
    private byte enemyIndex;

    public OfflinePvpVictoryEvent(byte enemyIndex) {
        this.enemyIndex = enemyIndex;
    }

    public byte getEnemyIndex() {
        return enemyIndex;
    }
}
