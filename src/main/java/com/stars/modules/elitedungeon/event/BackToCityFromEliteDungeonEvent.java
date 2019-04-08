package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/3/10.
 */
public class BackToCityFromEliteDungeonEvent extends Event {
    int eliteDungeonId;
    
    public BackToCityFromEliteDungeonEvent(int eliteDungeonId) {
        this.eliteDungeonId = eliteDungeonId;
    }

    public int getEliteDungeonId(){
    	return this.eliteDungeonId;
    }
}
