package com.stars.services.family.activities.bonfire;

import com.stars.modules.familyactivities.bonfire.BonfireActivityFlow;
import com.stars.modules.familyactivities.bonfire.FamilyBonfrieManager;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyQuestion;
import com.stars.services.ServiceHelper;
import com.stars.services.family.activities.bonfire.cache.BonFireQuestionCache;
import com.stars.util.TimeUtil;

/**
 * Created by wuyuxing on 2017/3/11.
 */
public class FireQuestionTask implements Runnable {
    @Override
    public void run() {
        if(!BonfireActivityFlow.isStarted()) return;    //不在篝火活动时间内
        if(!BonfireActivityFlow.isAnswerOpen()) return; //答题未开始

        if(FamilyBonFireServiceActor.questionIndex == -1){
            questionChange();//更换下一题
            return;
        }

        BonFireQuestionCache cache = FamilyBonFireServiceActor.getCurQuestionCache();
        if(cache == null) return;
        FamilyQuestion questionVo = FamilyBonfrieManager.getQuestion(cache.getQuestionId());
        if(questionVo == null) return;
        long now = System.currentTimeMillis();
        if(Math.abs(now - FamilyBonFireServiceActor.curQuestionEndTimes) <= 1.1 * TimeUtil.SECOND){//简单容错
            //本次答题结束
            ServiceHelper.familyBonFireService().questionEnd();
            return;
        }

        if(now - FamilyBonFireServiceActor.curQuestionEndTimes < FamilyBonfrieManager.QUESTIONS_INTERVAL * TimeUtil.SECOND){
            return;
        }
        questionChange();
    }

    private void questionChange(){
        if(FamilyBonFireServiceActor.questionIndex >=
                FamilyBonFireServiceActor.DailyFireQuestionCaches.size()){
            return;
        }
        FamilyBonFireServiceActor.questionIndex++;
        BonFireQuestionCache cache = FamilyBonFireServiceActor.getCurQuestionCache();
        if(cache == null) return;

        FamilyQuestion questionVo = FamilyBonfrieManager.getQuestion(cache.getQuestionId());
        if(questionVo == null) return;
        long now = System.currentTimeMillis();
        FamilyBonFireServiceActor.curQuestionBeginTimes = now;
        FamilyBonFireServiceActor.curQuestionEndTimes = now + questionVo.getTime() * TimeUtil.SECOND;

        //刷新题目,并下发给所有在篝火场景的玩家
        ServiceHelper.familyBonFireService().sendCurQuestionToOnline();
    }
}
