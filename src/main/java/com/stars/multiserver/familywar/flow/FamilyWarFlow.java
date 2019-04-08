package com.stars.multiserver.familywar.flow;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarService;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.util.LogUtil;
import com.stars.util.TimeUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaowenshuo on 2016/11/7.
 */
public class FamilyWarFlow extends ActivityFlow {

    //    public static final int STEP_GENERAL_START = 1;// 总流程开始
    public static final int STEP_LOCAL_KNOCKOUT_START = 1;// 本服淘汰赛开始
    public static final int STEP_LOCAL_KNOCKOUT_END = 2;// 本服淘汰赛结束
    public static final int STEP_REMOTE_QUALIFYING_START = 3;// 海选赛开始
    public static final int STEP_REMOTE_QUALIFYING_END = 4;// 海选赛结束
    public static final int STEP_REMOTE_KNOCKOUT_START = 5;// 跨服决赛开始
    public static final int STEP_REMOTE_KNOCKOUT_END = 6;// 跨服决赛结束
//    public static final int STEP_GENERAL_END = 8;// 总流程结束

    private FamilyWarService service;
    public static boolean isLocalRunning = false;
    public static boolean isMultiRunning = false;
    public static boolean isMultiServerRunning = false;
    public static int serverOpenDays = 1;

    @Override
    public String getActivityFlowName() {
        return "家族战总流程";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "活动未开启");
                break;
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                isLocalRunning = true;
                serverOpenDays = DataManager.getServerDays();
                if (localRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    LogUtil.info("familywar|本服挑战赛开始了");
                    createFlowInfo();
                    ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL, FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START, 0, true);
                    ServiceHelper.familyWarLocalService().getFixtures(false, "");
                }
                break;
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_END:
                if (localRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "活动未开启");
                }
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                isMultiRunning = true;
                serverOpenDays = DataManager.getServerDays();
                if (multiRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    LogUtil.info("familywar|跨服海选开始了:{}", isMultiRunning);
                    createFlowInfo();
                    MainRpcHelper.familyWarQualifyingService().qualifyFamilyWarServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
                    ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_START, 0, true);
                    MainRpcHelper.familyWarQualifyingService().updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                }
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_END:
                if (multiRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    LogUtil.info("familywar|跨服海选结束了:{}", isMultiRunning);
                    ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_END, 0, false);
                }
                break;
            case FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START:
                isMultiRunning = true;
                serverOpenDays = DataManager.getServerDays();
                if (multiRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    createFlowInfo();
                    LogUtil.info("familywar|跨服决赛开始了:{}", isMultiRunning);
                    MainRpcHelper.familyWarRemoteService().registerFamilyWarServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
                    ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START, 0, true);
                    MainRpcHelper.familyWarRemoteService().updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                }
                break;
            case FamilyWarFlow.STEP_REMOTE_KNOCKOUT_END:
                if (multiRunning()) {
                    FamilyWarConst.STEP_OF_GENERAL_FLOW = step;
                    LogUtil.info("familywar|跨服决赛结束了:{}", isMultiRunning);
                    ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_END, 0, false);
                }
                break;
            default:
                break;
        }
    }

    public void createFlowInfo() {
        // 初始化各种信息
        FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP = new ConcurrentHashMap<>();
        FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP.put(FamilyWarConst.W_TYPE_LOCAL,
                new FamilyWarFlowInfo(FamilyWarConst.W_TYPE_LOCAL, getFlowTime(FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START)));
        FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP.put(FamilyWarConst.W_TYPE_QUALIFYING,
                new FamilyWarFlowInfo(FamilyWarConst.W_TYPE_QUALIFYING, ActivityFlowUtil.getMMddHHmmStr(configMap.get(FamilyWarFlow.STEP_REMOTE_QUALIFYING_START))));
        FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP.put(FamilyWarConst.W_TYPE_REMOTE,
                new FamilyWarFlowInfo(FamilyWarConst.W_TYPE_REMOTE, ActivityFlowUtil.getMMddHHmmStr(configMap.get(FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START))));
    }

    private String getFlowTime(int step) {
        long time = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(configMap.get(step));
        return TimeUtil.toMMddHHmm(time);
    }

    public FamilyWarService getService() {
        return service;
    }

    public void setService(FamilyWarService service) {
        this.service = service;
    }

    public static boolean localRunning() {
        if (serverOpenDays < FamilyActWarManager.familywar_cycletime_min || serverOpenDays > FamilyActWarManager.familywar_cycletime_max) {
            return false;
        }
        if (!FamilyWarFlow.isLocalRunning) {
            return false;
        }
        return true;
    }

    public static boolean multiRunning() {
        LogUtil.info("familywar|multiRunning:{},serverOpendays:{}", isMultiRunning, serverOpenDays);
        if (serverOpenDays <= FamilyActWarManager.familywar_cycletime_max)
            return false;
        if (!isMultiRunning)
            return false;
        return true;
    }
}
