package com.stars.modules.opactsecondskill;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.opactsecondskill.listener.OpActSecondKillListener;
import com.stars.modules.opactsecondskill.prodata.SecKillVo;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.*;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class OpActSecondKillModuleFactory extends AbstractModuleFactory<OpActSecondKillModule> {

    public OpActSecondKillModuleFactory() {
        super(new OpActSecondKillPacketSet());
    }

    @Override
    public OpActSecondKillModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OpActSecondKillModule("限时抢购", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        OpActSecondKillListener listener = new OpActSecondKillListener((OpActSecondKillModule) module);
        eventDispatcher.reg(AddToolEvent.class, listener);
        eventDispatcher.reg(UseToolEvent.class, listener);
        eventDispatcher.reg(ForeShowChangeEvent.class, listener);
        eventDispatcher.reg(VipChargeEvent.class, listener);
//        eventDispatcher.reg(OpActSecondKillStateEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from seckill";
        Map<Integer, SecKillVo> secKillVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", SecKillVo.class, sql);
        if (secKillVoMap == null) {
            secKillVoMap = new HashMap<>();
        }
        Map<Integer, List<SecKillVo>> secKillVoGroupMap = new HashMap<>();
        Map<Integer, SecKillVo> checkGroupMap = new HashMap<>();
        Iterator iter = secKillVoMap.values().iterator();
        while (iter.hasNext()) {
            SecKillVo secKillVo = (SecKillVo) iter.next();
            List<SecKillVo> secKillVoList = secKillVoGroupMap.get(secKillVo.getGroup());
            if (secKillVoList == null) {
                secKillVoList = new ArrayList<>();
            }
            secKillVoList.add(secKillVo);
            secKillVoGroupMap.put(secKillVo.getGroup(), secKillVoList);
            if (secKillVo.getGroupCondition() == (byte) 1)
                checkGroupMap.put(secKillVo.getGroup(), secKillVo);
        }

        OpActSecondKillManager.setCheckGroupSecKillMap(checkGroupMap);
        OpActSecondKillManager.setSecKillMap(secKillVoMap);
        OpActSecondKillManager.setSecKillMapByGroup(secKillVoGroupMap);

        String dailyReset = DataManager.getCommConfig("seckill_reset", "0");
        if (dailyReset.equals("1")) {
            OpActSecondKillManager.setIsDailyReset(true);
        } else {
            OpActSecondKillManager.setIsDailyReset(false);
        }
    }
}
