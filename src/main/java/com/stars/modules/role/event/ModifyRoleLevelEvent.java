package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2017/2/16.
 */
public class ModifyRoleLevelEvent extends Event {
    private int value;// 值,自带符号,负数为减少,正数为增加

    public ModifyRoleLevelEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
