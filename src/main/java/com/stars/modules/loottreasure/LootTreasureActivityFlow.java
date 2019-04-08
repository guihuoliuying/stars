package com.stars.modules.loottreasure;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.loottreasure.event.ActivityStateTreasureEvent;
import com.stars.services.ServiceHelper;

import java.util.Calendar;

/**
 * 野外探宝活动触发;
 * Created by panzhenfeng on 2016/10/11.
 */
public class LootTreasureActivityFlow extends ActivityFlow {

    public static final int STEP_READY_START = 3; // 在STEP_START时触发，通知客户端活动准备开始;
    public static final int STEP_START_2 = 4; // 第二场开始
    public static final int STEP_END_2 = 5; // 第二场结束
    public static final int STEP_READY_START_2 = 6; // 第二场在STEP_START时触发，通知客户端活动准备开始;

    public LootTreasureActivityFlow(){

    }

    @Override
    public String getActivityFlowName() {
        return "lootTreasureActivityFlow";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        checkAcitvityTime(step, isRedo, -1);
    }

    public void checkAcitvityTime(int step, boolean isRedo, long roleId){
        boolean isStarting = false;
        switch (step) {
            case STEP_START_CHECK:
                if (between(STEP_START, STEP_END) || between(STEP_START_2, STEP_END_2)) {
                    isStarting = true;
                    dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_START, roleId);
                } else if (between(STEP_READY_START, STEP_START) || between(STEP_READY_START_2, STEP_START_2)) {
                    dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_READYSTART, roleId);
                }
                break;
            case STEP_START: case STEP_START_2:
                isStarting = true;
                dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_START, roleId);
                break;
            case STEP_END: case STEP_END_2:
                dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYSE_END, roleId);
                break;
            case STEP_READY_START: case STEP_READY_START_2:
                dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_READYSTART, roleId);
                break;
        }
        ServiceHelper.lootTreasureService().setLootTreasureActivityState(isStarting);
    }

    private void dispatchEvent(LootTreasureConstant.ACTIVITYSEGMENT activitysegment, long roleId){
        ActivityStateTreasureEvent activityStateTreasureEvent = new ActivityStateTreasureEvent(activitysegment);
        if(roleId > 0){
            ServiceHelper.roleService().notice(roleId, activityStateTreasureEvent);
        }else{
            ServiceHelper.roleService().noticeAll(activityStateTreasureEvent);
            if (activitysegment == LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_START) {
                ServiceHelper.chatService().announce("loot_tips_begin");
            }
        }
    }

    public long getEndTimeStamp(){
        long now = System.currentTimeMillis();
        long end1 = getTimeStamp(STEP_END);
        if (now <= end1) {
            return end1;
        }
        return getTimeStamp(STEP_END_2);
//        return getTimeStamp(STEP_END);
    }


    public long getStartTimeStamp(){
        long now = System.currentTimeMillis();
        if (now <= getTimeStamp(STEP_END)) {
            return getTimeStamp(STEP_START);
        }
        return getTimeStamp(STEP_START_2);
//        return getTimeStamp(STEP_START);
    }


    private long getTimeStamp(int step){
        String cronExpr = this.configMap.get(step);
        Calendar stepCalendar = ActivityFlowUtil.getTodayCalendar(cronExpr);
        return stepCalendar.getTimeInMillis();
    }
}

