package com.stars.multiserver.familywar.flow;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarRemoteService;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * step为1表示跨服家族战决赛取家族资格时间，详见下文决赛家族赛资格，同时取第一批精英成员资格，同时锁定家族成员
 * step为2表示跨服家族战决赛锁定精英成员名单时间
 * step为3表示决赛第一天32进16主界面显示图标开始时间
 * step为4表示第一天开始时间；
 * step为5表示第一天结束时间
 * step为6表示主界面图标消失，个人积分发奖，清分，通知族长，发放32强奖励，解锁17-32名公会
 * step为7表示决赛第二天主界面显示图标开始时间
 * step为8表示第二天开始时间；
 * step为9表示第二天结束时间
 * step为10表示主界面图标消失，个人积分发奖，清分，通知族长，发放16强奖励，解锁9-16名公会
 * step为11表示决赛第三天主界面显示图标开始时间
 * step为12表示第三天开始时间；
 * step为13表示第三天结束时间
 * step为14表示主界面图标消失，个人积分发奖，清分，通知族长，发放8强奖励，解锁5-8名公会
 * step为15表示决赛第四天主界面显示图标开始时间
 * step为16表示第四天开始时间；
 * step为17表示第四天结束时间
 * step为18表示主界面图标消失，个人积分发奖，清分，通知族长
 * step为19表示决赛第五天主界面显示图标开始时间
 * step为20表示第五天开始时间；
 * step为21表示第五天结束时间
 * step为22表示本服家族战结束，发4强奖，解锁4强家族
 * <p>
 * Created by chenkeyu on 2017-05-19.
 */
public class FamilyWarRemoteFlow extends ActivityFlow {
    public static final int STEP_START_REMOTE = 1;
    public static final int STEP_GENERATE_TEAM_SHEET = 2;

    public static final int STEP_BEFORE_32TO16 = 3;
    public static final int STEP_START_32TO16 = 4;
    public static final int STEP_END_32TO16 = 5;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8 = 6;

    public static final int STEP_BEFORE_16TO8 = 7;
    public static final int STEP_START_16TO8 = 8;
    public static final int STEP_END_16TO8 = 9;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4 = 10;

    public static final int STEP_BEFORE_8TO4 = 11;
    public static final int STEP_START_8TO4 = 12;
    public static final int STEP_END_8TO4 = 13;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2 = 14;

    public static final int STEP_BEFORE_4TO2 = 15;
    public static final int STEP_START_4TO2 = 16;
    public static final int STEP_END_4TO2 = 17;

    public static final int STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL = 18;

    public static final int STEP_BEFORE_FINNAL = 19;
    public static final int STEP_START_FINNAL = 20;
    public static final int STEP_END_FINNAL = 21;

    public static final int STEP_END_REMOTE = 22;

    private int battleType;

    public void setBattleType(int battleType) {
        this.battleType = battleType;
        LogUtil.info("familywar|flow battleType:{}", this.battleType);
    }

    @Override
    public String getActivityFlowName() {
        return "跨服决赛赛程";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW = step;
        FamilyWarRemoteService service = ServiceHelper.familyWarRemoteService();
        LogUtil.info("familywar|跨服决赛，当前战斗类型:{},步数:{}", battleType, step);
        if (!FamilyWarFlow.isMultiServerRunning) {
            return;
        }
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_OVER) {
            LogUtil.info("familywar|家族战跨服决赛已结束！");
            return;
        }
        switch (step) {
            case STEP_START_CHECK:
                break;
            case STEP_START_REMOTE:
                LogUtil.info("1、跨服家族战决赛取家族资格时间");
                service.startRemote(FamilyWarUtil.getFamilyWarServerId());
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                service.updateFlowInfo(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarConst.SHOW_APPLY_BUTTON);
                break;
            case STEP_GENERATE_TEAM_SHEET:
                LogUtil.info("2、表示跨服家族战决赛锁定精英成员名单时间");
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_BEFORE_32TO16:
                LogUtil.info("3、表示决赛第一天32进16主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_32TO16));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_32TO16:
                LogUtil.info("4、表示第一天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_32TO16:
                LogUtil.info("5、表示第一天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8:
                LogUtil.info("6、表示主界面图标消失，个人积分发奖，清分，通知族长，发放32强奖励，解锁17-32名公会");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_16TO8));
                break;
            case STEP_BEFORE_16TO8:
                LogUtil.info("7、表示决赛第二天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_16TO8));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_16TO8:
                LogUtil.info("8、表示第二天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_16TO8:
                LogUtil.info("9、表示第二天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4:
                LogUtil.info("10、表示主界面图标消失，个人积分发奖，清分，通知族长，发放16强奖励，解锁9-16名公会");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_8TO4));
                break;
            case STEP_BEFORE_8TO4:
                LogUtil.info("11、表示决赛第三天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_8TO4));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_8TO4:
                LogUtil.info("12、表示第三天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_8TO4:
                LogUtil.info("13、表示第三天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2:
                LogUtil.info("14、表示主界面图标消失，个人积分发奖，清分，通知族长，发放8强奖励，解锁5-8名公会");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_4TO2));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_BEFORE_4TO2:
                LogUtil.info("15、表示决赛第四天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_4TO2));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_4TO2:
                LogUtil.info("16、表示第四天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_4TO2:
                LogUtil.info("17、表示第四天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL:
                LogUtil.info("18、表示主界面图标消失，个人积分发奖，清分，通知族长");
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.sendAward_ResetPoints_NoticeMater(step, remainder(STEP_START_FINNAL));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_BEFORE_FINNAL:
                LogUtil.info("19、表示决赛第五天主界面显示图标开始时间");
                service.AsyncFihterEntityAndLockFamily(FamilyWarUtil.getFamilyWarServerId());
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), remainder(STEP_START_FINNAL));
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_START_FINNAL:
                LogUtil.info("20、表示第五天开始时间");
                service.startBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "进行中");
                break;
            case STEP_END_FINNAL:
                LogUtil.info("21、表示第五天结束时间");
                service.endBattle(FamilyWarUtil.getFamilyWarServerId(), battleType);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
                break;
            case STEP_END_REMOTE:
                LogUtil.info("22、表示跨服家族战结束，发4强奖，解锁4强家族");
                service.updateFlowInfo(FamilyWarUtil.getFamilyWarServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarConst.DISAPPEAR_APPLY_BUTTON);
                service.sendAward_ResetPoints_NoticeMater(step, 0);
                service.sendMainIcon2All(FamilyWarUtil.getFamilyWarServerId(), 0);
                service.updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
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
