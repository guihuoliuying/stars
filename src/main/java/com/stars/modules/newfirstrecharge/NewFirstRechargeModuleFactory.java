package com.stars.modules.newfirstrecharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.newfirstrecharge.listenner.NewFirstRechargeListenner;
import com.stars.modules.newfirstrecharge.prodata.NewFirstRecharge;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRechargeModuleFactory extends AbstractModuleFactory<NewFirstRechargeModule> {
    public NewFirstRechargeModuleFactory() {
        super(new NewFirstRechargePackets());
    }

    @Override
    public void loadProductData() throws Exception {
        List<NewFirstRecharge> newFirstRecharges = DBUtil.queryList(DBUtil.DB_PRODUCT, NewFirstRecharge.class, String.format("select * from newfirstrecharge where activitytype=%s", getActivityType()));
        Collections.sort(newFirstRecharges);
        Map<Integer, NewFirstRecharge> newFirstRechargeMap = new HashMap<>();
        for (NewFirstRecharge newFirstRecharge : newFirstRecharges) {
            newFirstRechargeMap.put(newFirstRecharge.getDay(), newFirstRecharge);
        }
        NewFirstRechargeManager.newFirstRechargeMap = newFirstRechargeMap;
    }

    @Override
    public NewFirstRechargeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewFirstRechargeModule("新首充每日活动", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        NewFirstRechargeListenner newFirstRechargeListenner = new NewFirstRechargeListenner((NewFirstRechargeModule) module);
        eventDispatcher.reg(VipChargeEvent.class, newFirstRechargeListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, newFirstRechargeListenner);
        eventDispatcher.reg(OperateActivityEvent.class, newFirstRechargeListenner);
        eventDispatcher.reg(ActLoopResetEvent.class, newFirstRechargeListenner);
    }

    public int getActivityType() {
        return OperateActivityConstant.ActType_NewFirstRecharge;
    }
}
