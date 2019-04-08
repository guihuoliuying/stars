package com.stars.modules.family.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/7.
 */
public class FamilyRedPacketGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args == null && args.length == 0) {
            throw new IllegalArgumentException("");
        }
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        switch (args[0]) {
            case "add":
                ServiceHelper.familyRedPacketService().addRedPacket(familyModule.getAuth(), 100);
                break;
            case "give":
                ServiceHelper.familyRedPacketService().giveRedPacket(familyModule.getAuth());
                break;
            case "seize":
                if (ServiceHelper.familyRedPacketService().seizeRedPacket(familyModule.getAuth(), Long.parseLong(args[1]))) {
                    DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
                    ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
                    toolModule.addAndSend(dropModule.executeDrop(108, 1,false), EventType.FAMILYSIZE.getCode());
                }
                break;
        }
    }
}
