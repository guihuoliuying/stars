package com.stars.modules.operateactivity;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.operateactivity.gm.OperateActivityGmHandler;
import com.stars.modules.operateactivity.listener.OperateActivityListener;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class OperateActivityModuleFactory extends AbstractModuleFactory<OperateActivityModule> {
    public OperateActivityModuleFactory() {
        super(new OperateActivityPacketSet());
    }

    OperateActivityFlow flow;

    @Override
    public OperateActivityModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new OperateActivityModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void init() throws Exception {
        OperateActivityManager.registerOpMoudle();
        OperateActivityManager.registerOpCheck();
        GmManager.reg("op", new OperateActivityGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        OperateActivityListener listener = new OperateActivityListener(module);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(ForeShowChangeEvent.class, listener);
        eventDispatcher.reg(OperateActivityEvent.class, listener);
        eventDispatcher.reg(OperateActivityFlowEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {
        loadOperateAct();
        initConfig();
        initFlow();
    }

    private void loadOperateAct() throws SQLException {
        String sql = "select * from `operateact`; ";
        Map<Integer, OperateActVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "operateactid", OperateActVo.class, sql);
        OperateActivityManager.setOperateActVoMap(map);
    }

    private void initConfig() {
        Map<Integer, Integer> operateActResignCost = new HashMap<>();

        String resignCostStr = DataManager.getCommConfig("operateact_resigncost");
        if (resignCostStr != null && (!resignCostStr.equals("")) && (!resignCostStr.equals("0"))) {
            String[] sts = resignCostStr.split("\\,");
            String[] ts;
            for (String tmp : sts) {
                ts = tmp.split("\\+");
                if (ts.length >= 2) {
                    operateActResignCost.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
                }
            }
        }

        OperateActivityManager.operateActResignCost = operateActResignCost;
    }

    private void initFlow() throws Exception {
        if (flow != null) {
            flow.stop(SchedulerHelper.getScheduler());
        }
        flow = new OperateActivityFlow();
        Map<Integer, String> flowMap = new HashMap<>();
        //flowMap.put(OperateActivityConstant.FLOW_STEP_NEW_DAY, "0 0 0 * * ?");//每天零点触发
        flowMap.put(OperateActivityConstant.FLOW_STEP_MINUTE, "0 * * * * ?");//每分钟触发
        flowMap.put(OperateActivityConstant.FLOW_STEP_NEW_DAY, "2 0 0 * * ?");//每天零点后2秒触发
        flow.init(SchedulerHelper.getScheduler(), flowMap);
    }
}

