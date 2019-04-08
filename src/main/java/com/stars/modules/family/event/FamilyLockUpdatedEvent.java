package com.stars.modules.family.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/11/1.
 */
public class FamilyLockUpdatedEvent extends Event {

    private long familyId;
    private byte isLock;
    private boolean isLockAll;

    public FamilyLockUpdatedEvent(long familyId, byte isLock) {
        this.familyId = familyId;
        this.isLock = isLock;
    }

    public FamilyLockUpdatedEvent(boolean isLockAll) {
        this.isLockAll = isLockAll;
//        this.isLock = isLockAll;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public byte getIsLock() {
        return isLock;
    }

    public void setIsLock(byte isLock) {
        this.isLock = isLock;
    }

    public boolean isLockAll() {
        return isLockAll;
    }

    public void setLockAll(boolean lockAll) {
        isLockAll = lockAll;
    }
}
