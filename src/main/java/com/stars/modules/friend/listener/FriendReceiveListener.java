package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendReceiveFlowerEvent;

/**
 * Created by chenkeyu on 2017/2/20 15:36
 */
public class FriendReceiveListener implements EventListener {
    private FriendModule module;

    public FriendReceiveListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendReceiveFlowerEvent flowerEvent = (FriendReceiveFlowerEvent) event;
        module.syncReceiveFlowerRecord(flowerEvent.getRolePo(),flowerEvent.getRecordPo());
    }
}
