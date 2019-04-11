package com.stars.modules.serverLog;

import com.stars.core.event.Event;
import com.stars.core.gmpacket.giftpackage.GiftLogEvent;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.email.event.EmailLogEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.vip.event.VipLevelupEvent;

public class ServerLogListener extends AbstractEventListener<ServerLogModule> {
    public ServerLogListener(ServerLogModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof VipLevelupEvent) {
            module().doVipLevelUpEvent((VipLevelupEvent) event);
        } else if (event instanceof RoleLevelUpEvent) {
            module().doRoleLevelUpEvent((RoleLevelUpEvent) event);
        } else if (event instanceof FightScoreChangeEvent) {
            module().doFightScoreChangeEvent((FightScoreChangeEvent) event);
        } else if (event instanceof EmailLogEvent) {
            module().doEmailLog((EmailLogEvent) event);
        } else if (event instanceof GiftLogEvent) {
            module().doGiftLog((GiftLogEvent) event);
        } else if (event instanceof SpecialAccountEvent) {
            module().doSpecialAccountEvent(((SpecialAccountEvent) event).getRoleId(), ((SpecialAccountEvent) event).getContent(), ((SpecialAccountEvent) event).isSelf());
        }
    }
}
