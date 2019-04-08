package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/12/10.
 */
public class FriendCanReceiveVigorEvent extends Event {
    private boolean flag;
    public FriendCanReceiveVigorEvent(boolean flag){
        this.flag = flag;
    }
    public boolean isFlag(){
        return flag;
    }
}
