package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class ReduceRoleResourceEvent extends Event {
    private int resourceId;// 资源的类型，对应item表的id
    private long value;// 自带符号

    public ReduceRoleResourceEvent(int resourceId, long value) {
        this.resourceId = resourceId;
        this.value = value;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
