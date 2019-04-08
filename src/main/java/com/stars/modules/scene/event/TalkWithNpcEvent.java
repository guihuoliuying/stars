package com.stars.modules.scene.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/7/26.
 */
public class TalkWithNpcEvent extends Event {
    private int npcId;

    public TalkWithNpcEvent(int npcId) {
        this.npcId = npcId;
    }

    public int getNpcId() {
        return npcId;
    }
}
