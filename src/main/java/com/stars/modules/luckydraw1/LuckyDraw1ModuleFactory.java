package com.stars.modules.luckydraw1;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw1.listenner.LuckyDraw1Listenner;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw1ModuleFactory extends AbstractModuleFactory<LuckyDraw1Module> {
    public LuckyDraw1ModuleFactory() {
        super(new LuckyDraw1PacketSet());
    }


    @Override
    public LuckyDraw1Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyDraw1Module("幸运抽奖1", id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckypumpaward where type=%s;";
        Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyPumpAwardVo.class,String.format(sql, OperateActivityConstant.ActType_LuckyDraw1) );
        LuckyDraw1Manager.luckyPumpAwardMap = luckyPumpAwardMap;
        List<LuckyPumpAwardVo> luckyPumpAwardList = new ArrayList<>(luckyPumpAwardMap.values());
        Collections.sort(luckyPumpAwardList, new Comparator<LuckyPumpAwardVo>() {
            @Override
            public int compare(LuckyPumpAwardVo o1, LuckyPumpAwardVo o2) {
                return o2.getId()-o1.getId();
            }
        });
        LuckyDraw1Manager.luckyPumpAwardList = luckyPumpAwardList;
        String luckyPumpMoney = DataManager.getCommConfig("luckypump_money1");
        LuckyDraw1Manager.luckyPumpMoney=luckyPumpMoney;
        String[] group = luckyPumpMoney.split("\\|");
        int moneyLimit = Integer.parseInt(group[0]);
        Map<Integer, Integer> reward = StringUtil.toMap(group[1], Integer.class, Integer.class, '+', '|');
        Map<Integer, Map<Integer, Integer>> moneyDrawReward = new HashMap<>();
        moneyDrawReward.put(moneyLimit, reward);
        LuckyDraw1Manager.moneyDrawReward = moneyDrawReward;
        Map<Integer, Integer> luckyDrawSwitch = StringUtil.toMap(DataManager.getCommConfig("luckypump_switch1"), Integer.class, Integer.class, '+', '|');
        LuckyDraw1Manager.luckyDrawSwitch = luckyDrawSwitch;
        LuckyDraw1Manager.luckyDrawNumlimit = Integer.parseInt(DataManager.getCommConfig("luckypump_numlimit1"));
        LuckyDraw1Manager.luckyDrawFreeTimes = Integer.parseInt(DataManager.getCommConfig("luckypump_gratistime1"));
        int luckyDrawConsumeUnit = Integer.parseInt(DataManager.getCommConfig("luckypump_consume1"));
        LuckyDraw1Manager.luckyDrawConsumeUnit = luckyDrawConsumeUnit;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyDraw1Listenner luckyDraw1Listenner = new LuckyDraw1Listenner((LuckyDraw1Module) module);
        eventDispatcher.reg(OperateActivityEvent.class, luckyDraw1Listenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyDraw1Listenner);
        eventDispatcher.reg(AddToolEvent.class, luckyDraw1Listenner);
    }
}
