package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendInitEvent;
import com.stars.modules.friend.packet.ClientBlacker;

import java.util.ArrayList;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendInitListener implements EventListener {

    private FriendModule module;

    public FriendInitListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendInitEvent initEvent = (FriendInitEvent) event;
        module.innerSetFriendList(initEvent.getFriendList());
        module.innerSetBlackList(initEvent.getBlackList());
        module.innerSetApplyList(initEvent.getApplyList());
//        module.innerSetVigorList(initEvent.getVigorList());

        // 发送黑名单列表
        ClientBlacker packet = new ClientBlacker(ClientBlacker.SUBTYPE_BLACKER_SIMPLE_LIST);
        packet.setBlackerIdList(new ArrayList<Long>(initEvent.getBlackList()));
        module.send(packet);
    }
}
