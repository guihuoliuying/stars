package com.stars.modules.deityweapon.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2017/2/9.
 */
public class ActiveDeityWeaponAchieveEvent extends Event {
    private byte type;

    public ActiveDeityWeaponAchieveEvent(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
