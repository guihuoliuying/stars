package com.stars.modules.buddy.pojo;

import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyGuard;

/**
 * Created by huwenjun on 2017/8/30.
 */
public class BuddyGuardPo {
    private int position;// '唯一标识',
    private int groupId;//组id
    private int status;

    public BuddyGuardPo(int position, int groupId, int status) {
        this.position = position;
        this.groupId = groupId;
        this.status = status;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BuddyGuard getBuddyGuard() {
        return BuddyManager.buddyGuardGroupMap.get(groupId).get(position-1);
    }
}
