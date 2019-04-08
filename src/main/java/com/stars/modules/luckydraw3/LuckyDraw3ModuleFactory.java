package com.stars.modules.luckydraw3;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw3.listenner.LuckyDraw3Listenner;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw3ModuleFactory extends AbstractModuleFactory<LuckyDraw3Module> {
    public LuckyDraw3ModuleFactory() {
        super(new LuckyDraw3PacketSet());
    }


    @Override
    public LuckyDraw3Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyDraw3Module("幸运抽奖1", id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckypumpaward where type=%s;";
        Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyPumpAwardVo.class,String.format(sql, OperateActivityConstant.ActType_LuckyDraw3) );
        LuckyDraw3Manager.luckyPumpAwardMap = luckyPumpAwardMap;
        List<LuckyPumpAwardVo> luckyPumpAwardList = new ArrayList<>(luckyPumpAwardMap.values());
        Collections.sort(luckyPumpAwardList, new Comparator<LuckyPumpAwardVo>() {
            @Override
            public int compare(LuckyPumpAwardVo o1, LuckyPumpAwardVo o2) {
                return o2.getId()-o1.getId();
            }
        });
        LuckyDraw3Manager.luckyPumpAwardList = luckyPumpAwardList;
        String luckyPumpMoney = DataManager.getCommConfig("luckypump_money3");
        LuckyDraw3Manager.luckyPumpMoney=luckyPumpMoney;
        String[] group = luckyPumpMoney.split("\\|");
        int moneyLimit = Integer.parseInt(group[0]);
        Map<Integer, Integer> reward = StringUtil.toMap(group[1], Integer.class, Integer.class, '+', '|');
        Map<Integer, Map<Integer, Integer>> moneyDrawReward = new HashMap<>();
        moneyDrawReward.put(moneyLimit, reward);
        LuckyDraw3Manager.moneyDrawReward = moneyDrawReward;
        Map<Integer, Integer> luckyDrawSwitch = StringUtil.toMap(DataManager.getCommConfig("luckypump_switch3"), Integer.class, Integer.class, '+', '|');
        LuckyDraw3Manager.luckyDrawSwitch = luckyDrawSwitch;
        LuckyDraw3Manager.luckyDrawNumlimit = Integer.parseInt(DataManager.getCommConfig("luckypump_numlimit3"));
        LuckyDraw3Manager.luckyDrawFreeTimes = Integer.parseInt(DataManager.getCommConfig("luckypump_gratistime3"));
        int luckyDrawConsumeUnit = Integer.parseInt(DataManager.getCommConfig("luckypump_consume3"));
        LuckyDraw3Manager.luckyDrawConsumeUnit = luckyDrawConsumeUnit;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyDraw3Listenner luckyDraw3Listenner = new LuckyDraw3Listenner((LuckyDraw3Module) module);
        eventDispatcher.reg(OperateActivityEvent.class, luckyDraw3Listenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyDraw3Listenner);
        eventDispatcher.reg(AddToolEvent.class, luckyDraw3Listenner);
    }
}
