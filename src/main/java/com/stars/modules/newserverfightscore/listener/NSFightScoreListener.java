package com.stars.modules.newserverfightscore.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.MConst;
import com.stars.modules.newserverfightscore.NewServerFightModule;
import com.stars.modules.newserverfightscore.event.NSFSHistoryRankUpdateEvent;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.event.FightScoreChangeEvent;

/**
 * Created by liuyuheng on 2017/1/9.
 */
public class NSFightScoreListener extends AbstractEventListener<NewServerFightModule> {
    public NSFightScoreListener(NewServerFightModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        // 角色战力改变 || 昨日排行榜更新(重置)
        if (event instanceof FightScoreChangeEvent || event instanceof NSFSHistoryRankUpdateEvent) {
            //module().signCalRedPoint(MConst.NewServerFightScore, RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD);
        }else if (event instanceof OperateActivityFlowEvent) {
			OperateActivityFlowEvent operateActivityFlowEvent = (OperateActivityFlowEvent)event;
			if (operateActivityFlowEvent.getStepType() == OperateActivityConstant.FLOW_STEP_NEW_DAY) {
				module().signCalRedPoint(MConst.NewServerFightScore, RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD);
			}
		}
    }
}
