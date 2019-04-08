package com.stars.modules.positionsync;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.friend.event.FriendDelFriendEvent;
import com.stars.modules.friend.event.FriendInitEvent;
import com.stars.modules.friend.event.FriendNewFriendEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.marry.event.MarryEvent;
import com.stars.services.marry.userdata.Marry;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/28.
 */
public class PositionSyncModule extends AbstractModule {

    public PositionSyncModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("位置同步", id, self, eventDispatcher, moduleMap);
    }

    public void onEvent(Event event) {
        if (event instanceof FriendInitEvent) {
            FriendInitEvent e = (FriendInitEvent) event;
            ServiceHelper.arroundPlayerService().addFriendId(id(), e.getFriendList());
            return;
        }
        if (event instanceof FriendNewFriendEvent) {
            FriendNewFriendEvent e = (FriendNewFriendEvent) event;
            ServiceHelper.arroundPlayerService().addFriendId(id(), Arrays.<Long>asList(e.getFriendId()));
            return;
        }
        if (event instanceof FriendDelFriendEvent) {
            FriendDelFriendEvent e = (FriendDelFriendEvent) event;
            ServiceHelper.arroundPlayerService().delFriendId(id(), Arrays.<Long>asList(e.getFriendId()));
            return;
        }
        if (event instanceof MarryEvent) {
            MarryEvent e = (MarryEvent) event;
            Marry marry = e.getMarry();
            long coupleId = 0;
            if (marry != null) {
                coupleId = marry.getMan() == id() ? e.getMarry().getWoman() : e.getMarry().getMan();
            }
            ServiceHelper.arroundPlayerService().updateCoupleId(id(), coupleId);
            return;
        }
        if (event instanceof FamilyAuthUpdatedEvent) {
            FamilyAuthUpdatedEvent e = (FamilyAuthUpdatedEvent) event;
            FamilyAuth auth = new FamilyAuth(e.getFamilyId(), e.getFamilyName(), e.getFamilyLevel(), id(), "", e.getPost());
            ServiceHelper.arroundPlayerService().updateFamilyAuth(id(), auth);
            return;
        }
    }

}
