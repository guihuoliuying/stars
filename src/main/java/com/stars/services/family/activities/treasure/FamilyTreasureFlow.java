package com.stars.services.family.activities.treasure;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.familyactivities.treasure.FamilyTreasureConst;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017/2/10 11:57
 */
public class FamilyTreasureFlow extends ActivityFlow {
    private static final int bossStart = 1;
    private static final int bossEnd = 2;
    private static final int boxStart = 3;
    private static final int boxEnd = 4;

    @Override
    public String getActivityFlowName() {
        return "family.treasure";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                if (between(1, 2)) {
                    start(FamilyTreasureConst.NORMAL_TREASURE, true);
                } else if (between(3, 4)) {
                    LogUtil.info("起服检查:家族探宝周末活动已经开始");
                    start(FamilyTreasureConst.SUNDAY_TREASURE, true);
                }
                break;
            case bossStart:
                start(FamilyTreasureConst.NORMAL_TREASURE, false);
                break;
            case bossEnd:
                end(FamilyTreasureConst.NORMAL_TREASURE, false);
                break;
            case boxStart:
                start(FamilyTreasureConst.SUNDAY_TREASURE, false);
                break;
            case boxEnd:
                end(FamilyTreasureConst.SUNDAY_TREASURE, false);
                break;
        }
    }

    private void start(int start, boolean startServer) {
        ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_TREASURE,
                FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
        ServiceHelper.familyTreasureService().start(start, startServer);
    }

    private static void end(int end, boolean startServer) {
        ServiceHelper.familyTreasureService().end(end, startServer);
    }
}
