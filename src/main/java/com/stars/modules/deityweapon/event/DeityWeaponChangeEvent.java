package com.stars.modules.deityweapon.event;

import com.stars.core.event.Event;

/**
 * 当前神兵改变事件;
 * Created by panzhenfeng on 2016/12/7.
 */
public class DeityWeaponChangeEvent  extends Event {
    private byte curDressdeityweaponType;

    public DeityWeaponChangeEvent(byte curDressdeityweaponType) {
        this.curDressdeityweaponType = curDressdeityweaponType;
    }

    public byte getCurDressdeityweaponType() {
        return curDressdeityweaponType;
    }
}

