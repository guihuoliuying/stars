package com.stars.multiserver.familywar.flow;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarQualifyingService;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * step为1表示跨服家族战海选取家族资格时间，详见下文海选家族资格，同时取第一批精英成员资格，同时锁定家族成员
 * step为2表示跨服家族战海选锁定精英成员名单时间
 * step为3表示海选第一天主界面显示图标开始时间
 * step为4表示第一天开始时间；
 * step为5表示第一天结束时间
 * step为6表示主界面图标消失，个人积分发奖，清分，通知族长
 * step为7表示海选第二天主界面显示图标开始时间
 * step为8表示第二天开始时间；
 * step为9表示第二天结束时间
 * step为10表示主界面图标消失，个人积分发奖，清分，通知族长
 * step为11表示海选第三天主界面显示图标开始时间
 * step为12表示第三天开始时间；
 * step为13表示第三天结束时间
 * step为14表示主界面图标消失，个人积分发奖，清分，通知族长
 * step为15表示海选第四天主界面显示图标开始时间
 * step为16表示第四天开始时间；
 * step为17表示第四天结束时间
 * step为18表示主界面图标消失，个人积分发奖，清分，通知族长
 * step为19表示海选第五天主界面显示图标开始时间
 * step为20表示第五天开始时间；
 * step为21表示第五天结束时间
 * step为22表示本服家族战结束，发奖，解锁家族
 * <p>
 * <p>
 * Created by chenkeyu on 2017-05-19.
 */
public class FamilyWarQualifyingFlow extends ActivityFlow {
    public static final int STEP_START_QUALIFYING = 1;
    public static final int STEP_GENERATE_TEAM_SHEET = 2;

    public static final int STEP_BEFORE_1ST = 3;
    public static final int STEP_START_1ST = 4;
    public static final int STEP_END_1ST = 5;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND = 6;

    public static final int STEP_BEFORE_2ND = 7;
    public static final int STEP_START_2ND = 8;
    public static final int STEP_END_2ND = 9;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD = 10;

    public static final int STEP_BEFORE_3RD = 11;
    public static final int STEP_START_3RD = 12;
    public static final int STEP_END_3RD = 13;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH = 14;

    public static final int STEP_BEFORE_4TH = 15;
    public static final int STEP_START_4TH = 16;
    public static final int STEP_END_4TH = 17;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH = 18;

    public static final int STEP_BEFORE_5TH = 19;
    public static final int STEP_START_5TH = 20;
    public static final int STEP_END_5TH = 21;

    public static final int STEP_END_QUALIFYING = 22;


    @Override
    public String getActivityFlowName() {
        return "跨服海选赛程";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW = step;
        FamilyWarQualifyingService service = ServiceHelper.familyWarQualifyingService();
        LogUtil.info("familywar|跨服海选，当前战斗类型:{}", step);
        if (!FamilyWarFlow.isMultiServerRunning) {
            return;
        }
        switch (step) {
            case STEP_START_CHECK:
                break;
            case STEP_START_QUALIFYING:
                LogUtil.info("1、跨服家族战海选取家族资格时间，同时取第一批精英成员资格，同时锁定家族成员");
                service.startQualifying(FamilyWarUtil.getFamilyWarServerId());
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                service.updateFlowInfo(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarConst.SHOW_APPLY_BUTTON);
                break;
            case STEP_GENERATE_TEAM_SHEET:
                LogUtil.info("2、表示跨服家族战海选锁定精英成员名单时间");
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_BEFORE_1ST:
                LogUtil.info("3、表示海选第一天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_1ST));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_1ST:
                LogUtil.info("4、表示第一天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_1ST);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_1ST:
                LogUtil.info("5、表示第一天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_1ST);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND:
                LogUtil.info("6、表示主界面图标消失，个人积分发奖，清分，通知族长");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_2ND));
                break;
            case STEP_BEFORE_2ND:
                LogUtil.info("7、表示海选第二天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_2ND));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_2ND:
                LogUtil.info("8、表示第二天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_2ND);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_2ND:
                LogUtil.info("9、表示第二天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_2ND);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD:
                LogUtil.info("10、表示主界面图标消失，个人积分发奖，清分，通知族长");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_3RD));
                break;
            case STEP_BEFORE_3RD:
                LogUtil.info("11、表示海选第三天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_3RD));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_3RD:
                LogUtil.info("12、表示第三天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_3RD);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_3RD:
                LogUtil.info("13、表示第三天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_3RD);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH:
                LogUtil.info("14、表示主界面图标消失，个人积分发奖，清分，通知族长");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_4TH));
                break;
            case STEP_BEFORE_4TH:
                LogUtil.info("15、表示海选第三天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_4TH));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_4TH:
                LogUtil.info("16、表示第四天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_4TH);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_4TH:
                LogUtil.info("17、表示第四天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_4TH);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH:
                LogUtil.info("18、表示主界面图标消失，个人积分发奖，清分，通知族长");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_5TH));
                break;
            case STEP_BEFORE_5TH:
                LogUtil.info("19、表示海选第五天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_5TH));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_5TH:
                LogUtil.info("20、表示第五天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_5Th);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_5TH:
                LogUtil.info("21、表示第五天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.Q_BATTLE_TYPE_5Th);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_END_QUALIFYING:
                LogUtil.info("22、解锁发奖");
                service.updateFlowInfo(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarConst.DISAPPEAR_APPLY_BUTTON);
                service.sendAward_ResetPoints_NoticeMater(step, 0);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                service.resetDataBase(FamilyWarUtil.getFamilyWarServerId());
                ServiceHelper.familywarRankService().resetTitle(MultiServerHelper.getServerId(), false);
                try {
                    stop(SchedulerHelper.getScheduler());
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
                break;
            default:
                break;
        }
    }
}
