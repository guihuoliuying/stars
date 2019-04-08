package com.stars.services.luckyturntable.cache;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class LuckyList implements Comparable<LuckyList> {
    private String roleName;
    private int itemId;
    private long time;
    private int count;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(roleName);
        buff.writeInt(itemId);
        buff.writeInt(count);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "LuckyList{" +
                "roleName='" + roleName + '\'' +
                ", itemId=" + itemId +
                ", time=" + time +
                ", count=" + count +
                '}';
    }

    @Override
    public int compareTo(LuckyList o) {
        if (this.time - o.time > 0) {
            return -1;
        } else if (this.time - o.time < 0) {
            return 1;
        } else if (this.itemId - o.itemId > 0) {
            return -1;
        } else if (this.itemId - o.itemId < 0) {
            return 1;
        } else if (this.count - o.count > 0) {
            return -1;
        } else if (this.count - o.count < 0) {
            return 1;
        } else {
            return this.roleName.compareTo(o.roleName);
        }
    }
}
