package com.stars.services.postsync;

import com.stars.services.family.FamilyAuth;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/24.
 */
public class PositionSyncRelation {

    private long roleId;
    private int vipLevel;
    private long coupleId;
    private Set<Long> friendIdSet;
    private FamilyAuth auth;

    public PositionSyncRelation(long roleId) {
        this.roleId = roleId;
        this.friendIdSet = new HashSet<>();
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public long getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(long coupleId) {
        this.coupleId = coupleId;
    }

    public Set<Long> getFriendIdSet() {
        return friendIdSet;
    }

    public void setFriendIdSet(Set<Long> friendIdSet) {
        this.friendIdSet = friendIdSet;
    }

    public FamilyAuth getAuth() {
        return auth;
    }

    public void setAuth(FamilyAuth auth) {
        this.auth = auth;
    }
}
