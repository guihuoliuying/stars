package com.stars.modules.countDown;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;

import java.util.Calendar;
import java.util.Map;

/**
 * @author zhanghaizhen
 * 活动预告(倒计时)
 * 2017-06-29
 */
public class CountDownModule extends AbstractModule implements OpActivityModule {

	public CountDownModule(long id, Player self, EventDispatcher eventDispatcher,
                           Map<String, Module> moduleMap) {
		super("CountDown", id, self, eventDispatcher, moduleMap);
	}

	@Override
	public void onInit(boolean isCreation) throws Throwable {
		super.onInit(isCreation);
	}
	
	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

	}
	
	@Override
	public int getCurShowActivityId() {
		int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_CountDown);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            }
        }
        return -1;
	}

	@Override
	public void onOffline() throws Throwable {
		if(getCurShowActivityId()==-1){
			return;
		}
	}

	@Override
	public byte getIsShowLabel() {
		return 0;
	}

}
