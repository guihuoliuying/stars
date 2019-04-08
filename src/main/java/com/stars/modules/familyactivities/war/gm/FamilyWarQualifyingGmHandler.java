package com.stars.modules.familyactivities.war.gm;

import com.stars.core.module.Module;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.gm.GmHandler;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-20.
 */
public class FamilyWarQualifyingGmHandler implements GmHandler {
    private int serverOpenDays = 1;

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FamilyWarConst.STEP_OF_GENERAL_FLOW = FamilyWarFlow.STEP_REMOTE_QUALIFYING_START;
        String cmd = args[0];
        switch (cmd) {
            case "start":
                LogUtil.info("Gm家族战开启流程");
                serverOpenDays = DataManager.getServerDays();
                MainRpcHelper.familyWarQualifyingService().qualifyFamilyWarServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
                ServiceHelper.familyWarService().startQualify(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING);
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_START, 1, true);
                MainRpcHelper.familyWarQualifyingService().startQualifying(FamilyWarUtil.getFamilyWarServerId());
                break;
            case "11":
            case "22":
            case "33":
            case "44":
            case "55":
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst
                        .W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_START, (Integer.parseInt(cmd) % 10) * 4, true);
                MainRpcHelper.familyWarQualifyingService().startBattle(FamilyWarUtil.getFamilyWarServerId(),
                        Integer.parseInt(cmd) % 10);
                break;
            case "10":
            case "20":
            case "30":
            case "40":
            case "50":
                LogUtil.info("Gm家族战开始战斗");
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst
                        .W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_START, (Integer.parseInt(cmd) % 10) * 4 + 1, true);
                MainRpcHelper.familyWarQualifyingService().endBattle(FamilyWarUtil.getFamilyWarServerId(),
                        Integer.parseInt(cmd) / 10);
                break;
            case "odd":
                FamilyActWarManager.familywar_pairstageodd = Integer.parseInt(args[1]);
                MainRpcHelper.familyWarQualifyingService().odd(FamilyWarUtil.getFamilyWarServerId(), FamilyActWarManager.familywar_pairstageodd);
                break;
            case "trigger":
                LogUtil.info("Gm家族战跨服海选拉起");
                ServiceHelper.familyWarService().startQualifyByDisaster(MultiServerHelper.getServerId());
                break;
        }
    }
}
