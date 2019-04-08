package com.stars.services.family.activities.invade;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.familyactivities.invade.event.FamilyActInvadeStartEvent;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvadeNotice;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceUtil;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyActInvadeFlow extends ActivityFlow {

    private static boolean isStarted = false;

    @Override
    public String getActivityFlowName() {
        return "family.invade";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        ServiceHelper.familyActEntryService().setOptions(
                ActConst.ID_FAMILY_INVADE, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
        switch (step) {
            case STEP_START_CHECK:
                if (between(1, 2)) {
                    LogUtil.info("起服检查:家族入侵活动已经开始");
                    start();
                }
                break;
            case STEP_START:
                if (isStarted)
                    return;
                LogUtil.info("家族入侵活动现在开始");
                start();
                break;
            case STEP_END:
                if (!isStarted)
                    return;
                end();
                break;
        }
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public static void start() {
        isStarted = true;
        ServiceHelper.familyActInvadeService().start();
        ServiceHelper.familyActEntryService().setOptions(
                ActConst.ID_FAMILY_INVADE, FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
        ServiceHelper.roleService().noticeAll(new FamilyActInvadeStartEvent());
    }

    public static void end() {
        isStarted = false;
        LogUtil.info("家族入侵活动时间结束");
        ServiceHelper.familyActInvadeService().timeOver();
        ServiceHelper.familyActEntryService().setOptions(
                ActConst.ID_FAMILY_INVADE, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
        ServiceUtil.sendPacketToOnline(new ClientFamilyInvadeNotice(ClientFamilyInvadeNotice.END), null);
    }
}
