package com.stars.modules.luckydraw2;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw2.listenner.LuckyDraw2Listenner;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.*;


/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw2ModuleFactory extends AbstractModuleFactory<LuckyDraw2Module> {
    public LuckyDraw2ModuleFactory() {
        super(new LuckyDraw2PacketSet());
    }


    @Override
    public LuckyDraw2Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyDraw2Module("幸运抽奖1", id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckypumpaward where type=%s;";
        Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyPumpAwardVo.class,String.format(sql, OperateActivityConstant.ActType_LuckyDraw2) );
        LuckyDraw2Manager.luckyPumpAwardMap = luckyPumpAwardMap;
        List<LuckyPumpAwardVo> luckyPumpAwardList = new ArrayList<>(luckyPumpAwardMap.values());
        Collections.sort(luckyPumpAwardList, new Comparator<LuckyPumpAwardVo>() {
            @Override
            public int compare(LuckyPumpAwardVo o1, LuckyPumpAwardVo o2) {
                return o2.getId()-o1.getId();
            }
        });
        LuckyDraw2Manager.luckyPumpAwardList = luckyPumpAwardList;
        String luckyPumpMoney = DataManager.getCommConfig("luckypump_money2");
        LuckyDraw2Manager.luckyPumpMoney=luckyPumpMoney;
        String[] group = luckyPumpMoney.split("\\|");
        int moneyLimit = Integer.parseInt(group[0]);
        Map<Integer, Integer> reward = StringUtil.toMap(group[1], Integer.class, Integer.class, '+', '|');
        Map<Integer, Map<Integer, Integer>> moneyDrawReward = new HashMap<>();
        moneyDrawReward.put(moneyLimit, reward);
        LuckyDraw2Manager.moneyDrawReward = moneyDrawReward;
        Map<Integer, Integer> luckyDrawSwitch = StringUtil.toMap(DataManager.getCommConfig("luckypump_switch2"), Integer.class, Integer.class, '+', '|');
        LuckyDraw2Manager.luckyDrawSwitch = luckyDrawSwitch;
        LuckyDraw2Manager.luckyDrawNumlimit = Integer.parseInt(DataManager.getCommConfig("luckypump_numlimit2"));
        LuckyDraw2Manager.luckyDrawFreeTimes = Integer.parseInt(DataManager.getCommConfig("luckypump_gratistime2"));
        int luckyDrawConsumeUnit = Integer.parseInt(DataManager.getCommConfig("luckypump_consume2"));
        LuckyDraw2Manager.luckyDrawConsumeUnit = luckyDrawConsumeUnit;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyDraw2Listenner luckyDraw2Listenner = new LuckyDraw2Listenner((LuckyDraw2Module) module);
        eventDispatcher.reg(OperateActivityEvent.class, luckyDraw2Listenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyDraw2Listenner);
        eventDispatcher.reg(AddToolEvent.class, luckyDraw2Listenner);
    }
}
