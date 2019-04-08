package com.stars.modules.collectphone;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.collectphone.gm.CollectPhoneGmHandler;
import com.stars.modules.collectphone.listenner.CollectPhoneListenner;
import com.stars.modules.collectphone.prodata.StepOperateAct;
import com.stars.modules.gm.GmManager;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/13.
 */
public class CollectPhoneModuleFactory extends AbstractModuleFactory<CollectPhoneModule> {
    public CollectPhoneModuleFactory() {
        super(new CollectPhonePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from stepoperateact where operateacttype=" + OperateActivityConstant.ActType_CollectPhone;
        CollectPhoneManager.stepOperateActList = DBUtil.queryList(DBUtil.DB_PRODUCT, StepOperateAct.class, sql);
        Map<Integer, StepOperateAct> stepOperateActMap = new HashMap<>();
        for (StepOperateAct stepOperateAct : CollectPhoneManager.stepOperateActList) {
            stepOperateActMap.put(stepOperateAct.getStep(), stepOperateAct);
        }
        CollectPhoneManager.stepOperateActMap = stepOperateActMap;
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("collectPhone", new CollectPhoneGmHandler());
    }

    @Override
    public CollectPhoneModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new CollectPhoneModule("手机号码收集", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        CollectPhoneListenner collectPhoneListenner = new CollectPhoneListenner((CollectPhoneModule) module);
        eventDispatcher.reg(OperateActivityEvent.class, collectPhoneListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, collectPhoneListenner);
    }
}
