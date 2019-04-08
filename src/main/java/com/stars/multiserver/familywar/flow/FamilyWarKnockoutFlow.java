package com.stars.multiserver.familywar.flow;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarLocalService;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.util.LogUtil;

/**
 * 淘汰赛流程
 * 1. 确定家族资格，开始报名/取消报名/确认名单，锁定家族
 * 2. 截止报名/取消报名/确认名单，生成参赛名单
 * 3. 四分之一决赛开始之前
 * 4. 四分之一决赛开始
 * 5. 四分之一决赛结束
 * 6. 个人积分排行榜发奖、个人积分重置，通知族长可以改精英名单
 * 7. 二分之一决赛开始之前
 * 8. 二分之一决赛开始
 * 9. 二分之一决赛结束
 * 10. 个人积分排行榜发奖、个人积分重置，通知族长可以改精英名单
 * 11. 决赛开始（含34名决赛）之前
 * 12. 决赛开始（含34名决赛）
 * 13. 决赛结束（含34名决赛）
 * 14. 发奖，解锁锁定家族
 * Created by zhaowenshuo on 2016/11/7.
 */
public class FamilyWarKnockoutFlow extends ActivityFlow {

    public static final int STEP_START_KNOCKOUT = 1;
    public static final int STEP_GENERATE_TEAM_SHEET = 2;

    public static final int STEP_BEFORE_QUARTER_FIANLS = 3;
    public static final int STEP_START_QUARTER_FINALS = 4;
    public static final int STEP_END_QUARTER_FINALS = 5;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI = 6;

    public static final int STEP_BEFORE_SEMI_FIANLS = 7;
    public static final int STEP_START_SEMI_FINALS = 8;
    public static final int STEP_END_SEMI_FINALS = 9;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS = 10;

    public static final int STEP_BEFORE_END_FIANLS = 11;
    public static final int STEP_START_FINALS = 12;
    public static final int STEP_END_FINALS = 13;

    public static final int STEP_END_KNOCKOUT = 14;

    private FamilyWarLocalService localService;


    @Override
    public String getActivityFlowName() {
        return "淘汰赛流程";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {

        localService = ServiceHelper.familyWarLocalService();
        if (!FamilyWarFlow.localRunning()) {
            return;
        }
        FamilyWarConst.STEP_OF_SUB_FLOW = step;
        switch (step) {
            case STEP_START_CHECK:
                break;
            case STEP_START_KNOCKOUT:
                LogUtil.info("1. 确定家族资格，开始报名/取消报名/确认名单，锁定家族");
                ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
//                ServiceHelper.familyWarService().updateFlowInfo(FamilyWarConst.W_TYPE_LOCAL, FamilyWarConst.WAR_START_WITH_QUALIFY);
                localService.start(MultiServerHelper.getServerId());
                localService.getFixtures(false, "");
                ServiceHelper.familyWarService().updateFlowInfo(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL, FamilyWarConst.SHOW_APPLY_BUTTON);
                break;
            case STEP_GENERATE_TEAM_SHEET:
                LogUtil.info("2. 截止报名/取消报名/确认名单，生成参赛名单");
//                localService.sendMainIcon2All(remainder(STEP_START_QUARTER_FINALS));
                localService.getFixtures(false, "");
                break;
            case STEP_BEFORE_QUARTER_FIANLS:
                LogUtil.info("3. 四分之一决赛开始之前");
                localService.AsyncFihterEntity();
                localService.sendMainIcon2All(remainder(STEP_START_QUARTER_FINALS));
                localService.getFixtures(false, "");
                break;
            case STEP_START_QUARTER_FINALS:
                LogUtil.info("4. 四分之一决赛开始");
                localService.startQuarterFinals();
                localService.sendMainIcon2All(0);
                localService.getFixtures(true, "进行中");
                break;
            case STEP_END_QUARTER_FINALS:
                LogUtil.info("5. 四分之一决赛结束");
                localService.endQuarterFinals();
                localService.sendMainIcon2All(0);
                localService.getFixtures(false, "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI:
                LogUtil.info("6. 个人积分排行榜发奖、个人积分重置，通知族长可以改精英名单");
                // TODO: 2017-05-04 图标消失
                localService.sendMainIcon2All(0);
                localService.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_SEMI_FINALS));
                break;
            case STEP_BEFORE_SEMI_FIANLS:
                LogUtil.info("7. 二分之一决赛开始之前");
                localService.AsyncFihterEntity();
                localService.sendMainIcon2All(remainder(STEP_START_SEMI_FINALS));
                localService.getFixtures(false, "");
                break;
            case STEP_START_SEMI_FINALS:
                LogUtil.info("8. 二分之一决赛开始");
                localService.startSemiFinals();
                localService.sendMainIcon2All(0);
                localService.getFixtures(true, "进行中");
                break;
            case STEP_END_SEMI_FINALS:
                LogUtil.info("9. 二分之一决赛结束");
                localService.endSemiFinals();
                localService.sendMainIcon2All(0);
                localService.getFixtures(false, "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS:
                LogUtil.info("10. 个人积分排行榜发奖、个人积分重置，通知族长可以改精英名单");
                // TODO: 2017-05-04 图标消失
                localService.sendMainIcon2All(0);
                localService.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_FINALS));
                break;
            case STEP_BEFORE_END_FIANLS:
                LogUtil.info("11. 决赛开始（含34名决赛）之前");
                localService.AsyncFihterEntity();
                localService.sendMainIcon2All(remainder(STEP_START_FINALS));
                localService.getFixtures(false, "");
                break;
            case STEP_START_FINALS:
                LogUtil.info("12. 决赛开始（含34名决赛）");
                localService.startFinal();
                localService.sendMainIcon2All(0);
                localService.getFixtures(true, "进行中");
                break;
            case STEP_END_FINALS:
                LogUtil.info("13. 决赛结束（含34名决赛）");
                localService.endFinal();
                localService.sendMainIcon2All(0);
                localService.getFixtures(false, "");
                break;
            case STEP_END_KNOCKOUT:
                LogUtil.info("14. 发奖，解锁锁定家族");
                localService.end(MultiServerHelper.getServerId());
                localService.sendAward_ResetPoints_NoticeMater(step, 0);
                localService.sendMainIcon2All(0);
                localService.getFixtures(false, "");
                ServiceHelper.familyWarService().updateFlowInfo(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL, FamilyWarConst.DISAPPEAR_APPLY_BUTTON);
                try {
                    stop(SchedulerHelper.getScheduler());
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
                break;
        }
    }

    public FamilyWarLocalService getLocalService() {
        return localService;
    }

    public void setLocalService(FamilyWarLocalService localService) {
        this.localService = localService;
    }
}
