package com.stars.modules.positionsync;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.friend.event.FriendDelFriendEvent;
import com.stars.modules.friend.event.FriendInitEvent;
import com.stars.modules.friend.event.FriendNewFriendEvent;
import com.stars.network.PlaceholderPacketSet;
import com.stars.services.marry.event.MarryEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/28.
 */
public class PositionSyncModuleFactory extends AbstractModuleFactory<PositionSyncModule> {

    public PositionSyncModuleFactory() {
        super(new PlaceholderPacketSet());
    }

    @Override
    public PositionSyncModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new PositionSyncModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        PositionSyncEventListener listener = new PositionSyncEventListener((PositionSyncModule) module);

        eventDispatcher.reg(FriendInitEvent.class, listener);
        eventDispatcher.reg(FriendNewFriendEvent.class, listener);
        eventDispatcher.reg(FriendDelFriendEvent.class, listener);
        eventDispatcher.reg(MarryEvent.class, listener);
        eventDispatcher.reg(FamilyAuthUpdatedEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {
        int maxSyncNum = DataManager.getCommConfig("operate_people_num", 20);
        double viewRadius = DataManager.getCommConfig("operate_people_radius", 250.0);
        int highVipLevel = DataManager.getCommConfig("operate_people_hightv", 10);

        PositionSyncManager.MaxSyncNum = maxSyncNum;
        PositionSyncManager.ViewRadius = viewRadius;
        PositionSyncManager.HighVipLevel = highVipLevel;
    }
}
