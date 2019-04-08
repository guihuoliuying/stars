package com.stars.modules.friend.event;

import com.stars.core.event.Event;

import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendInitEvent extends Event {

    private Set<Long> friendList;
    private Set<Long> blackList;
    private Set<Long> applyList;
//    private Set<Long> vigorList;
    public FriendInitEvent(Set<Long> friendList, Set<Long> blackList,Set<Long> applyList ) {
        this.friendList = friendList;
        this.blackList = blackList;
        this.applyList =applyList;
//        this.vigorList = vigorList;
    }

    public Set<Long> getFriendList() {
        return friendList;
    }

    public Set<Long> getBlackList() {
        return blackList;
    }

    public Set<Long> getApplyList(){ return applyList;}
//    public Set<Long> getVigorList(){return vigorList;}
}
