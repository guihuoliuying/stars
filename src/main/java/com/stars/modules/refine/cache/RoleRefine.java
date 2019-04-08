package com.stars.modules.refine.cache;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RoleRefine {
    private int itemId;
    private int count;
    private long roleId;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(itemId);
        buff.writeInt(count);
    }

    public RoleRefine(long roleId) {
        this.roleId = roleId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "RoleRefine{" +
                "itemId=" + itemId +
                ", count=" + count +
                ", roleId=" + roleId +
                '}';
    }
}
