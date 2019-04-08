package com.stars.modules.luckydraw4;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw4.listenner.LuckyDraw4Listenner;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw4ModuleFactory extends AbstractModuleFactory<LuckyDraw4Module> {
    public LuckyDraw4ModuleFactory() {
        super(new LuckyDraw4PacketSet());
    }


    @Override
    public LuckyDraw4Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyDraw4Module("幸运抽奖1", id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckypumpaward where type=%s;";
        Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyPumpAwardVo.class, String.format(sql, OperateActivityConstant.ActType_LuckyDraw4));
        LuckyDraw4Manager.luckyPumpAwardMap = luckyPumpAwardMap;
        List<LuckyPumpAwardVo> luckyPumpAwardList = new ArrayList<>(luckyPumpAwardMap.values());
        Collections.sort(luckyPumpAwardList, new Comparator<LuckyPumpAwardVo>() {
            @Override
            public int compare(LuckyPumpAwardVo o1, LuckyPumpAwardVo o2) {
                return o2.getId() - o1.getId();
            }
        });
        LuckyDraw4Manager.luckyPumpAwardList = luckyPumpAwardList;
        String luckyPumpMoney = DataManager.getCommConfig("luckypump_money4");
        LuckyDraw4Manager.luckyPumpMoney = luckyPumpMoney;
        String[] group = luckyPumpMoney.split("\\|");
        int moneyLimit = Integer.parseInt(group[0]);
        Map<Integer, Integer> reward = StringUtil.toMap(group[1], Integer.class, Integer.class, '+', '|');
        Map<Integer, Map<Integer, Integer>> moneyDrawReward = new HashMap<>();
        moneyDrawReward.put(moneyLimit, reward);
        LuckyDraw4Manager.moneyDrawReward = moneyDrawReward;
        Map<Integer, Integer> luckyDrawSwitch = StringUtil.toMap(DataManager.getCommConfig("luckypump_switch4"), Integer.class, Integer.class, '+', '|');
        LuckyDraw4Manager.luckyDrawSwitch = luckyDrawSwitch;
        LuckyDraw4Manager.luckyDrawNumlimit = Integer.parseInt(DataManager.getCommConfig("luckypump_numlimit4"));
        LuckyDraw4Manager.luckyDrawFreeTimes = Integer.parseInt(DataManager.getCommConfig("luckypump_gratistime4"));
        int luckyDrawConsumeUnit = Integer.parseInt(DataManager.getCommConfig("luckypump_consume4"));
        LuckyDraw4Manager.luckyDrawConsumeUnit = luckyDrawConsumeUnit;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyDraw4Listenner luckyDraw4Listenner = new LuckyDraw4Listenner((LuckyDraw4Module) module);
        eventDispatcher.reg(OperateActivityEvent.class, luckyDraw4Listenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyDraw4Listenner);
        eventDispatcher.reg(AddToolEvent.class, luckyDraw4Listenner);
    }
}
