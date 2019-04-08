package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghaizhen on 2017/6/16.
 */
public class DragonBallChangeEvent extends Event {
    List<String> dragonBallList = new ArrayList<>();
    public DragonBallChangeEvent(List<String> dragonBallList){
        this.dragonBallList = dragonBallList;
    }

    public List<String> getDragonBallList() {
        return dragonBallList;
    }

    public void setDragonBallList(List<String> dragonBallList) {
        this.dragonBallList = dragonBallList;
    }
}
