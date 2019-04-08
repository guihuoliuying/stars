package com.stars.modules.serverLog;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.giftpackage.GiftLogEvent;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.email.event.EmailLogEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarLogEvent;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.weeklygift.event.WeeklyGiftEvent;
import com.stars.services.fightingmaster.event.NoticeMainServerAddTool;

import java.util.Map;

public class ServerLogModuleFactory extends AbstractModuleFactory<ServerLogModule> {

    public ServerLogModuleFactory() {
        super(null);
    }

    @Override
    public ServerLogModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ServerLogModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ServerLogListener listener = new ServerLogListener((ServerLogModule) module);
        eventDispatcher.reg(VipLevelupEvent.class, listener);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(NoticeMainServerAddTool.class, listener);
        eventDispatcher.reg(EmailLogEvent.class, listener);
        eventDispatcher.reg(GiftLogEvent.class, listener);
        eventDispatcher.reg(SpecialAccountEvent.class, listener);
        eventDispatcher.reg(FamilyWarLogEvent.class, listener);
        eventDispatcher.reg(PushActivedEvent.class, listener);
        eventDispatcher.reg(WeeklyGiftEvent.class, listener);
    }
}
