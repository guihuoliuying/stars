package com.stars.services.luckydraw;

import com.stars.modules.luckydraw.LuckyDrawManagerFacade;
import com.stars.modules.luckydraw.pojo.LuckyDrawAnnounce;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.LinkedList;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawServiceActor extends ServiceActor implements LuckyDrawService {
    private static int top = 10;
    private LinkedList<LuckyDrawAnnounce> luckyDrawAnnounces = new LinkedList<>();
    private LinkedList<LuckyDrawAnnounce> luckyDraw1Announces = new LinkedList<>();
    private LinkedList<LuckyDrawAnnounce> luckyDraw2Announces = new LinkedList<>();
    private LinkedList<LuckyDrawAnnounce> luckyDraw3Announces = new LinkedList<>();
    private LinkedList<LuckyDrawAnnounce> luckyDraw4Announces = new LinkedList<>();

    @Override

    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.LuckyDrawService, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void luckyAnnounce(LuckyDrawAnnounce luckyDrawAnnounce) {
        getLuckyAnnounceTop10(luckyDrawAnnounce.getActType()).addFirst(luckyDrawAnnounce);
        if (getLuckyAnnounceTop10(luckyDrawAnnounce.getActType()).size() > top) {
            getLuckyAnnounceTop10(luckyDrawAnnounce.getActType()).removeLast();
        }
        LuckyPumpAwardVo luckyPumpAwardVo = LuckyDrawManagerFacade.getLuckyPumpAwardMap(luckyDrawAnnounce.getActType()).get(luckyDrawAnnounce.getAwardId());
        ServiceHelper.chatService().announce(luckyPumpAwardVo.getDesc(), luckyDrawAnnounce.getRoleName());
    }

    @Override
    public LinkedList<LuckyDrawAnnounce> getLuckyAnnounceTop10(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw:
                return luckyDrawAnnounces;
            case OperateActivityConstant.ActType_LuckyDraw1:
                return luckyDraw1Announces;
            case OperateActivityConstant.ActType_LuckyDraw2:
                return luckyDraw2Announces;
            case OperateActivityConstant.ActType_LuckyDraw3:
                return luckyDraw3Announces;
            case OperateActivityConstant.ActType_LuckyDraw4:
                return luckyDraw4Announces;
        }
        return null;
    }

}