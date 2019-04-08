package com.stars.modules.luckydraw;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.luckydraw.gm.LuckyDrawGmHandler;
import com.stars.modules.luckydraw.listenner.LuckyDrawListenner;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawModuleFactory extends AbstractModuleFactory<LuckyDrawModule> {
    public LuckyDrawModuleFactory() {
        super(new LuckyDrawPacketSet());
    }


    @Override
    public LuckyDrawModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyDrawModule("幸运抽奖", id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("luckydraw", new LuckyDrawGmHandler());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckypumpaward where type=%s;";
        Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyPumpAwardVo.class,String.format(sql, OperateActivityConstant.ActType_LuckyDraw) );
        LuckyDrawManager.luckyPumpAwardMap = luckyPumpAwardMap;
        List<LuckyPumpAwardVo> luckyPumpAwardList = new ArrayList<>(luckyPumpAwardMap.values());
        Collections.sort(luckyPumpAwardList, new Comparator<LuckyPumpAwardVo>() {
            @Override
            public int compare(LuckyPumpAwardVo o1, LuckyPumpAwardVo o2) {
                return o2.getId()-o1.getId();
            }
        });
        LuckyDrawManager.luckyPumpAwardList = luckyPumpAwardList;
        String luckyPumpMoney = DataManager.getCommConfig("luckypump_money");
        LuckyDrawManager.luckyPumpMoney=luckyPumpMoney;
        String[] group = luckyPumpMoney.split("\\|");
        int moneyLimit = Integer.parseInt(group[0]);
        Map<Integer, Integer> reward = StringUtil.toMap(group[1], Integer.class, Integer.class, '+', '|');
        Map<Integer, Map<Integer, Integer>> moneyDrawReward = new HashMap<>();
        moneyDrawReward.put(moneyLimit, reward);
        LuckyDrawManager.moneyLimit=moneyLimit;
        LuckyDrawManager.moneyDrawReward = moneyDrawReward;
        Map<Integer, Integer> luckyDrawSwitch = StringUtil.toMap(DataManager.getCommConfig("luckypump_switch"), Integer.class, Integer.class, '+', '|');
        LuckyDrawManager.luckyDrawSwitch = luckyDrawSwitch;
        LuckyDrawManager.luckyDrawNumlimit = Integer.parseInt(DataManager.getCommConfig("luckypump_numlimit"));
        LuckyDrawManager.luckyDrawFreeTimes = Integer.parseInt(DataManager.getCommConfig("luckypump_gratistime"));
        int luckyDrawConsumeUnit = Integer.parseInt(DataManager.getCommConfig("luckypump_consume"));
        LuckyDrawManager.luckyDrawConsumeUnit = luckyDrawConsumeUnit;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyDrawListenner luckyDrawListenner = new LuckyDrawListenner((LuckyDrawModule) module);
        eventDispatcher.reg(VipChargeEvent.class, luckyDrawListenner);
        eventDispatcher.reg(OperateActivityEvent.class, luckyDrawListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyDrawListenner);
        eventDispatcher.reg(AddToolEvent.class, luckyDrawListenner);
    }
}
