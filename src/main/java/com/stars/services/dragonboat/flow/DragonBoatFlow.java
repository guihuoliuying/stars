package com.stars.services.dragonboat.flow;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.dragonboat.DragonBoatConst;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class DragonBoatFlow extends ActivityFlow {

    @Override
    public String getActivityFlowName() {
        return "端午节赛龙舟活动日流程";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        ServiceHelper.opDragonBoatService().updateStepTime();
        int currentStep = (step - 1) % 5 + 1;
        LogUtil.info("初始化本轮龙舟：{}", currentStep);
        switch (currentStep) {
            case DragonBoatConst.BET_ON: {
                ServiceHelper.opDragonBoatService().startBetOn();
            }
            break;
            case DragonBoatConst.RACING: {
                ServiceHelper.opDragonBoatService().startRacing();
            }
            break;
            case DragonBoatConst.WAITING: {
                ServiceHelper.opDragonBoatService().startWaiting();
            }
            break;
            case DragonBoatConst.SHOW: {
                ServiceHelper.opDragonBoatService().startShow();
            }
            break;
            case DragonBoatConst.FINISH: {
                LogUtil.info("龙舟回合结束");
                ServiceHelper.opDragonBoatService().finishRound();
            }
        }
    }

}
