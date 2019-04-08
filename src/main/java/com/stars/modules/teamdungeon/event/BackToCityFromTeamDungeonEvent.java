package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/11/1.
 */
public class BackToCityFromTeamDungeonEvent extends Event {
    int teamDungeonId;
    
    public BackToCityFromTeamDungeonEvent(int teamDungeonId) {
        this.teamDungeonId = teamDungeonId;
    }

    public int getTeamDungeonId(){
    	return this.teamDungeonId;
    }
}
