package com.stars.modules.soul.limit;

import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/16.
 */
public abstract class AbstractLimit {
    private int type;
    private int value;
    public final static int TYPE_UPGRADE = 1;
    public final static int TYPE_BREAK = 2;

    public AbstractLimit(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public abstract boolean limit(Map<String, Module> moduleMap, boolean sendTips, int type);

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
