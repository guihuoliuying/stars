package com.stars.modules.luckydraw.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.luckydraw.LuckyDrawModule;
import com.stars.modules.luckydraw.packet.ClientLuckyDrawPacket;
import com.stars.modules.luckydraw.pojo.LuckyDrawAnnounce;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by huwenjun on 2017/8/11.
 */
public class LuckyDrawGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        LuckyDrawModule luckyDrawModule = (LuckyDrawModule) moduleMap.get(MConst.LuckyDraw);
        String arg = args[0];
        String[] group = arg.split("=");
        String action = group[0];
        switch (action) {
            case "main": {
                luckyDrawModule.reqMainUiData();
            }
            break;
            case "time": {
                int time = Integer.parseInt(group[1]);
                luckyDrawModule.reqLuckyDraw(time);
            }
            break;
            case "announce": {
                int actType = Integer.parseInt(group[1]);
                LuckyDrawAnnounce luckyDrawAnnounce = new LuckyDrawAnnounce(1, "张三", 1, actType);
                ServiceHelper.luckyDrawService().luckyAnnounce(luckyDrawAnnounce);
            }
            break;
            case "report": {
                int maxTime = 100000;
                if (group.length > 1) {
                    maxTime = Integer.parseInt(group[1]);
                }
                luckyDrawModule.report(maxTime);
            }
            break;
            case "close": {
                int actType = Integer.parseInt(group[1]);
                ClientLuckyDrawPacket clientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_ACTIVITY_STATUS,actType);
                luckyDrawModule.send(clientLuckyDrawPacket);
            }
            break;
            case "redpoint": {
                luckyDrawModule.signRedPoint();
            }
            break;
        }
    }
}
