package com.stars.modules.familyactivities.bonfire;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.familyactivities.bonfire.event.BonfireActEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.util.DateUtil;

/**
 * 家族篝火流程控制
 * Created by zhouyaohui on 2016/10/11.
 */
public class BonfireActivityFlow extends ActivityFlow {

    public final static int CLOSE = 0;
    public final static int OPEN = 1;
    public static boolean ANSWER_OPEN = true;//答题开始

    private static volatile int actState = CLOSE;
    private static volatile int activityVersion = 0;

    @Override
    public String getActivityFlowName() {
        return "family.bonfire";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                if (between(1, 2)) {
                    /** 启动服务是，处在活动进行时间 篝火这边不需要处理 */
                    updateState(OPEN);
                } else {
                    updateState(CLOSE);
                }
                if (between(3, 2)) {
                    ANSWER_OPEN = true;
                }
                break;
            case 1:
                updateState(OPEN);
                borcastMessage(OPEN);
                break;
            case 2:
                updateState(CLOSE);
                borcastMessage(CLOSE);
                break;
            case 3:
                ANSWER_OPEN = true;
                break;
        }
    }

    /**
     * 广播
     *
     * @param state
     */
    private void borcastMessage(int state) {
        ServiceHelper.roleService().noticeAll(new BonfireActEvent(state));
    }

    /**
     * 更新状态
     *
     * @param state
     */
    private void updateState(int state) {
        synchronized (BonfireActivityFlow.class) {
            activityVersion = DateUtil.getSecondTime();
            actState = state;
        }
        if (state == OPEN) {
            start();
            ServiceHelper.familyActEntryService().setOptions(
                    ActConst.ID_FAMILY_BONFIRE, FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
        } else {
            end();
            ANSWER_OPEN = false;
            ServiceHelper.familyActEntryService().setOptions(
                    ActConst.ID_FAMILY_BONFIRE, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
        }
    }

    /**
     * 获取当前活动状态
     *
     * @return
     */
    public static int getState() {
        return actState;
    }

    /**
     * 活动期数
     *
     * @return
     */
    public static int getActivityVersion() {
        return activityVersion;
    }

    public static void start() {
        ServiceHelper.familyBonFireService().start();
    }

    public static void end() {
        ServiceHelper.familyBonFireService().end();
    }

    public static boolean isStarted() {
        return actState == OPEN;
    }

    /**
     * 是否开始答题
     */
    public static boolean isAnswerOpen() {
        return ANSWER_OPEN;
    }
}
