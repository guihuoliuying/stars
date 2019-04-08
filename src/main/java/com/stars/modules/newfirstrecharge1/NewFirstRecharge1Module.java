package com.stars.modules.newfirstrecharge1;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRecharge1Module extends NewFirstRechargeModule {
    public NewFirstRecharge1Module(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }
    @Override
    public int getActivityType() {
        return OperateActivityConstant.ActType_NewFirstRecharge1;
    }
    @Override
    public EventType getEventType() {
        return EventType.NEW_FIRST_RECHARGE1;
    }
    @Override
    public String getModuleName() {
        return MConst.NewFirstRechargeModule1;
    }
    @Override
    public int getRedPointId() {
        return RedPointConst.NEW_FIRST_RECHARGE1;
    }
    @Override
    public int getEmailTemplateId() {
        return 30022;
    }
}
