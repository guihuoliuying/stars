package com.stars.modules.familyactivities.war.gm;

import com.stars.core.module.Module;
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
 * Created by chenkeyu on 2017-06-07.
 */
public class FamilyWarRemoteGmHandler implements GmHandler {
    public static int battleType;

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FamilyWarConst.STEP_OF_GENERAL_FLOW = FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START;
        String cmd = args[0];
        switch (cmd) {
            case "start":
                LogUtil.info("Gm家族战跨服决赛开启流程");
                MainRpcHelper.familyWarRemoteService().registerFamilyWarServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(),
                        FamilyWarConst.W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START, 1, true);
                ServiceHelper.familyWarService().startQualify(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE);
                MainRpcHelper.familyWarRemoteService().startRemote(FamilyWarUtil.getFamilyWarServerId());
                MainRpcHelper.familyWarRemoteService().updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case "s":
                LogUtil.info("Gm家族战跨服决赛开始战斗:{}", battleType);
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst
                        .W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START, switchFlow(battleType), true);
                MainRpcHelper.familyWarRemoteService().startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                break;
            case "e":
                LogUtil.info("Gm家族战跨服决赛结束战斗:{}", battleType);
                ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst
                        .W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START, switchFlow(battleType) + 1, true);
                MainRpcHelper.familyWarRemoteService().endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                break;
            case "trigger":
                LogUtil.info("Gm家族战跨服决赛拉起");
                ServiceHelper.familyWarService().startRemoteByDisaster(MultiServerHelper.getServerId());
                break;
            case "debug":
                String[] tmp = args[1].split("\\+");
                int period = Integer.parseInt(tmp[0]);
                int size = Integer.parseInt(tmp[1]);
                MainRpcHelper.familywarRankService().debug(FamilyWarUtil.getFamilyWarServerId(), period, size);
                break;
        }
    }

    private static int switchFlow(int battleType) {
        switch (battleType) {
            case FamilyWarConst.R_BATTLE_TYPE_32TO16:
                return 4;
            case FamilyWarConst.R_BATTLE_TYPE_16TO8:
                return 8;
            case FamilyWarConst.R_BATTLE_TYPE_8TO4:
                return 12;
            case FamilyWarConst.R_BATTLE_TYPE_4TO2:
                return 16;
            default:
                return 20;

        }
    }
}
