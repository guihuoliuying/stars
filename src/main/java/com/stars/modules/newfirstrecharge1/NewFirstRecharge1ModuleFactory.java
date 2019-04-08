package com.stars.modules.newfirstrecharge1;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.gm.GmManager;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModule;
import com.stars.modules.newfirstrecharge.gm.NewFirstRechargeGmHandler;
import com.stars.modules.newfirstrecharge.listenner.NewFirstRechargeListenner;
import com.stars.modules.newfirstrecharge.prodata.NewFirstRecharge;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRecharge1ModuleFactory extends AbstractModuleFactory<NewFirstRecharge1Module> {


    public NewFirstRecharge1ModuleFactory() {
        super(new NewFirstRecharge1Packets());
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("newfirstrecharge", new NewFirstRechargeGmHandler());
    }

    @Override
    public void loadProductData() throws Exception {
        List<NewFirstRecharge> newFirstRecharges = DBUtil.queryList(DBUtil.DB_PRODUCT, NewFirstRecharge.class, String.format("select * from newfirstrecharge where activitytype=%s", getActivityType()));
        Collections.sort(newFirstRecharges);
        Map<Integer, NewFirstRecharge> newFirstRechargeMap = new HashMap<>();
        for (NewFirstRecharge newFirstRecharge : newFirstRecharges) {
            newFirstRechargeMap.put(newFirstRecharge.getDay(), newFirstRecharge);
        }
        NewFirstRecharge1Manager.newFirstRechargeMap = newFirstRechargeMap;
    }

    @Override
    public NewFirstRecharge1Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewFirstRecharge1Module("新首充每日活动", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        NewFirstRechargeListenner newFirstRechargeListenner = new NewFirstRechargeListenner((NewFirstRechargeModule) module);
        eventDispatcher.reg(RoleLevelUpEvent.class, newFirstRechargeListenner);
        eventDispatcher.reg(OperateActivityEvent.class, newFirstRechargeListenner);
    }

    public int getActivityType() {
        return OperateActivityConstant.ActType_NewFirstRecharge1;
    }
}
