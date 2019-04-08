package com.stars.modules.serverLog;

import com.stars.core.event.Event;
import com.stars.core.gmpacket.giftpackage.GiftLogEvent;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.email.event.EmailLogEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarLogEvent;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.weeklygift.event.WeeklyGiftEvent;
import com.stars.services.fightingmaster.event.NoticeMainServerAddTool;

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
        } else if (event instanceof NoticeMainServerAddTool) {
            byte staus = ((NoticeMainServerAddTool) event).getStatus();
            module().doNoticeMainServerAddTool(staus);
        } else if (event instanceof EmailLogEvent) {
            module().doEmailLog((EmailLogEvent) event);
        } else if (event instanceof GiftLogEvent) {
            module().doGiftLog((GiftLogEvent) event);
        } else if (event instanceof SpecialAccountEvent) {
            module().doSpecialAccountEvent(((SpecialAccountEvent) event).getRoleId(), ((SpecialAccountEvent) event).getContent(), ((SpecialAccountEvent) event).isSelf());
        } else if (event instanceof FamilyWarLogEvent) {
            FamilyWarLogEvent logEvent = (FamilyWarLogEvent) event;
            module().dynamic_4_Log(ThemeType.FAMILY_WAR.getThemeId(), logEvent.getType(), logEvent.getRank(),
                    logEvent.getKill(), logEvent.getIntegral(), logEvent.getSuccess(), logEvent.getWarType(), logEvent.getBattleType(), logEvent.getItemMap());
        } else if (event instanceof PushActivedEvent) {
//            module().logPush((PushActivedEvent)event);
            module().logPrecisionPush(((PushActivedEvent) event).getPushInfoMap());
        } else if (event instanceof WeeklyGiftEvent) {
            WeeklyGiftEvent giftEvent = (WeeklyGiftEvent) event;
            module().dynamic_4_Log(ThemeType.DYNAMIC_WEEKLYGIFT.getThemeId(), giftEvent.getGiftId(), giftEvent.getCharge(), giftEvent.getItemMap());
        }
    }
}
