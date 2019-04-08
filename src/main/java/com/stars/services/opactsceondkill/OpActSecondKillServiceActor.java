package com.stars.services.opactsceondkill;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.opactsecondskill.OpActSecondKillManager;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.opactsceondkill.pojo.OpActSkStepTime;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-09-20.
 */
public class OpActSecondKillServiceActor extends ServiceActor implements OpActSecondKillService {
    private OpActSecondKillFlow sceondKillFlow;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.OpActSecondKillService, this);
        sceondKillFlow = new OpActSecondKillFlow();
        sceondKillFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_SCEOND_KILL));
        getInitStep();
    }

    @Override
    public void printState() {

    }

    private void getInitStep() {
        Map<Integer, String> stepMap = DataManager.getActivityFlowConfig(ActConst.ID_SCEOND_KILL);
        if (stepMap == null) throw new IllegalArgumentException("限时秒杀没有时间配置数据");
        List<OpActSkStepTime> opActSkStepTimeList = new LinkedList<>();
        for (Map.Entry<Integer, String> entry : stepMap.entrySet()) {
            OpActSkStepTime opActSkStepTime = new OpActSkStepTime();
            opActSkStepTime.setStep(entry.getKey());
            opActSkStepTime.setTime(ActivityFlowUtil.getTimeInMillisByCronExprByWeek(entry.getValue()));
            opActSkStepTimeList.add(opActSkStepTime);
        }
        Collections.sort(opActSkStepTimeList);
        long now = System.currentTimeMillis();
        int nextStep = 0;
        for (OpActSkStepTime time : opActSkStepTimeList) {
            if (time.getTime() > now) {
                nextStep = time.getStep();
                break;
            }
        }
        if (nextStep == 0){
            updateSceondKillState(false, nextStep);
            return;
        }
        if (nextStep % 2 == 0) {
            updateSceondKillState(true, nextStep);
        } else {
            updateSceondKillState(false, nextStep);
        }
    }

    @Override
    public void updateSceondKillState(boolean isOpen, int nextStep) {
        if (!isOpen) {
//            SystemRecordMap.update("opActSceondKillResetTimestamp", resetTimeStamp);
            OpActSecondKillManager.stopTimeStamp = 0L;
            OpActSecondKillManager.isOpActSecKillOpen = false;
        } else {
            Map<Integer, String> stepMap = DataManager.getActivityFlowConfig(ActConst.ID_SCEOND_KILL);
            if (stepMap != null && stepMap.containsKey(nextStep)) {
                long nextTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(stepMap.get(nextStep));
                OpActSecondKillManager.stopTimeStamp = nextTime;
                OpActSecondKillManager.isOpActSecKillOpen = isOpen;
            } else {
                OpActSecondKillManager.isOpActSecKillOpen = false;
                OpActSecondKillManager.stopTimeStamp = 0L;
                LogUtil.info("updateSceondKillState|状态转换出现问题！|没有nextStep:{} 的数据", nextStep);
            }
        }
        LogUtil.info("isOpen:{},nextStep:{}", OpActSecondKillManager.isOpActSecKillOpen, nextStep);
    }
}
