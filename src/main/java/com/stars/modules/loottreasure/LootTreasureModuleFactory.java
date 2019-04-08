package com.stars.modules.loottreasure;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.loottreasure.event.ActivityStateTreasureEvent;
import com.stars.modules.loottreasure.event.NotifyEnterLootTreasureEvent;
import com.stars.modules.loottreasure.listener.ActivityStateTreasureListener;
import com.stars.modules.loottreasure.listener.BackCityLootTreasureListener;
import com.stars.modules.loottreasure.listener.NotifyEnterLootTreasureListener;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.modules.scene.event.RequestExitFightEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 野外夺宝模块工厂;
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasureModuleFactory extends AbstractModuleFactory<LootTreasureModule> {

    public LootTreasureModuleFactory() {
        super(new LootTreasurePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        initLootSection();
        initConfig();
    }

    /**
     * 初始化部分配置;
     */
    private void initConfig() {
        //宝箱上限;
        String[] roomFlagLimitArr = DataManager.getCommConfig("loot_boxlabelcount").split("\\+");
        Map<Byte, Integer> roomFlagLimitMap = new ConcurrentHashMap<>();
        for(byte i = 0, len = (byte)roomFlagLimitArr.length; i<len; i++){
            roomFlagLimitMap.put(i, Integer.parseInt(roomFlagLimitArr[i]));
        }
        LootTreasureManager.roomFlagBoxLimitMap = roomFlagLimitMap;
        //宝箱掉落区域;
        List<LootTreasureRangeParam> loseParamList = new ArrayList<>();
        String[] loseCountArr = DataManager.getCommConfig("loot_losecount").split("\\|");
        String[] itemArr ;
        String[] rangeArr ;
        String[] loseArr;
        int minBoxCount;
        int maxBoxCount;
        int loseMinCount;
        int loseMaxCount;
        for(int i = 0, len = loseCountArr.length; i<len; i++){
            itemArr = loseCountArr[i].split("\\+");
            rangeArr = itemArr[0].split("-");
            loseArr = itemArr[1].split("-");
            minBoxCount = maxBoxCount = Integer.parseInt(rangeArr[0]);
            if(rangeArr.length > 1){
                maxBoxCount = Integer.parseInt(rangeArr[1]);
            }
            loseMinCount = loseMaxCount = Integer.parseInt(loseArr[0]);
            if(loseArr.length > 1){
                loseMaxCount = Integer.parseInt(loseArr[1]);
            }
            loseParamList.add(new LootTreasureRangeParam(minBoxCount, maxBoxCount, loseMinCount, loseMaxCount));
        }
        LootTreasureManager.loseParamList = loseParamList;
        //击杀获得宝箱的权值公式值;
        String[] rateArr = DataManager.getCommConfig("loot_getodds").split("\\+");
        LootTreasureManager.getBoxFormularParamA = Integer.parseInt(rateArr[0]);
        LootTreasureManager.getBoxFormularParamB = Integer.parseInt(rateArr[1]);
        //活动时间;
        LootTreasureManager.PVE_WAIT_TIME = Integer.valueOf(DataManager.getCommConfig("loot_beginready"))*1000;
        LootTreasureManager.PVE_FIGHT_TIME = Integer.valueOf(DataManager.getCommConfig("loot_bosslivetime"))*1000;
        LootTreasureManager.PVP_WAIT_TIME = Integer.valueOf(DataManager.getCommConfig("loot_waittime"))*1000;
        LootTreasureManager.PVP_FIGHT_TIME = Integer.valueOf(DataManager.getCommConfig("loot_pvplasttime"))*1000;
        LootTreasureManager.PVP_OVER_WAIT_TIME = Integer.valueOf(DataManager.getCommConfig("loot_pvpovereff"));
        LootTreasureManager.OVER_EMAIL_TEMPLATE_ID = Integer.valueOf(DataManager.getCommConfig("loot_awardmail"));
        LootTreasureManager.PVP_SWITCH_ROOM_CD = Integer.valueOf(DataManager.getCommConfig("loot_cooldown"))*1000;
        LootTreasureManager.PERSON_LIMITCOUNT_PER_ROOM = Integer.valueOf(DataManager.getCommConfig("loot_roomcount"));
        //做下临时的提示;
        if(LootTreasureManager.PERSON_LIMITCOUNT_PER_ROOM > (LootTreasureManager.CAMP_MAX - LootTreasureManager.CAMP_MIN + 1)){
            LootTreasureManager.log("房间上限数量分配过大，阵营会不够用的：房间上限="+LootTreasureManager.PERSON_LIMITCOUNT_PER_ROOM+"  阵营数="+(LootTreasureManager.CAMP_MAX - LootTreasureManager.CAMP_MIN + 1));
        }
        String[] conditionArr = DataManager.getCommConfig("loot_roomcondition").split("\\|");
        Map<Byte, LootTreasureRangeParam> conditionMap = new HashMap<>();
        String[] conditonItemArr;
        LootTreasureRangeParam lootTreasureRangeParam ;
        for(int i = 0, len = conditionArr.length; i<len; i++){
            conditonItemArr = conditionArr[i].split("\\+");
            lootTreasureRangeParam = new LootTreasureRangeParam(Integer.parseInt(conditonItemArr[0]), Integer.parseInt(conditonItemArr[1]));
            conditionMap.put((byte)i, lootTreasureRangeParam);
        }
        LootTreasureManager.pvpSwitchRoomConditionMap = conditionMap;
    }

    private void initLootSection() throws SQLException {
        String lootSectionSql = "select * from lootsection";
        List<LootSectionVo> lootSectionVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, LootSectionVo.class, lootSectionSql);
        LootTreasureManager.setLootSectionVoList(lootSectionVoList);
    }

    @Override
    public LootTreasureModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new LootTreasureModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(ActivityStateTreasureEvent.class, new ActivityStateTreasureListener(module));
        eventDispatcher.reg(RequestExitFightEvent.class, new BackCityLootTreasureListener(module));
        eventDispatcher.reg(NotifyEnterLootTreasureEvent.class, new NotifyEnterLootTreasureListener(module));
    }

}
