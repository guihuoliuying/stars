package com.stars.modules.familyactivities.treasure;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.treasure.event.FamilyTreasureStageEvent;
import com.stars.modules.familyactivities.treasure.event.LeaveOrKickOutFamilyEvent;
import com.stars.modules.familyactivities.treasure.gm.FamilyTreasureGmHandler;
import com.stars.modules.familyactivities.treasure.listener.FamilyTreasureListener;
import com.stars.modules.familyactivities.treasure.listener.LeaveOrKickOutFamilyListener;
import com.stars.modules.familyactivities.treasure.prodata.FTBuffVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdvawardVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdvendVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdventureVo;
import com.stars.modules.gm.GmManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/10 11:29
 */
public class FamilyTreasureModuleFactory extends AbstractModuleFactory<FamilyTreasureModule> {
    public FamilyTreasureModuleFactory() {
        super(new FamilyTreasurePacket());
    }

    @Override
    public void loadProductData() throws Exception {
        loadFamilyAdventureVo();
        loadFamilyAdvendVo();
        loadFamilyAdawardVo();
        loadCommondefine();
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("updaterank", new FamilyTreasureGmHandler());
    }

    @Override
    public FamilyTreasureModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyTreasureModule("家族探宝", id, self, eventDispatcher, map);
    }

    private void loadFamilyAdventureVo() throws SQLException {
        Map<Integer, Map<Integer, FamilyAdventureVo>> familyAdventureVoMap = new HashMap<>();
        List<FamilyAdventureVo> voList = DBUtil.queryList(DBUtil.DB_PRODUCT, FamilyAdventureVo.class, "select * from familyadventure");
        for (FamilyAdventureVo adventureVo : voList) {
            Map<Integer, FamilyAdventureVo> tmpMap = familyAdventureVoMap.get(adventureVo.getAdvLevel());
            if (tmpMap == null) {
                tmpMap = new HashMap<>();
                familyAdventureVoMap.put(adventureVo.getAdvLevel(), tmpMap);
            }
            tmpMap.put(adventureVo.getStep(), adventureVo);
        }
        FamilyTreasureManager.familyAdventureVoMap = familyAdventureVoMap;
    }

    private void loadFamilyAdvendVo() throws SQLException {
        Map<String, FamilyAdvendVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "endcondition", FamilyAdvendVo.class, "select * from familyadvend");
        FamilyTreasureManager.familyAdvendVoMap = map;
    }

    private void loadFamilyAdawardVo() throws SQLException {
        Map<Integer, Map<FamilyAdvawardVo, Integer>> advAwardMap = new HashMap<>();
        List<FamilyAdvawardVo> voList = DBUtil.queryList(DBUtil.DB_PRODUCT, FamilyAdvawardVo.class, "select * from familyadvaward");
        for (FamilyAdvawardVo advawardVo : voList) {
            Map<FamilyAdvawardVo, Integer> damageDrop = advAwardMap.get(advawardVo.getGroup());
            if (damageDrop == null) {
                damageDrop = new HashMap<>();
                advAwardMap.put(advawardVo.getGroup(), damageDrop);
            }
            damageDrop.put(advawardVo, advawardVo.getDropId());
        }
        FamilyTreasureManager.familyAdvawardVoMap = advAwardMap;
    }

    private void loadCommondefine() {
        FamilyTreasureManager.familyadventure_count = Integer.parseInt(DataManager.getCommConfig("familyadventure_count"));
        FamilyTreasureManager.familyadventure_sundaycount = Integer.parseInt(DataManager.getCommConfig("familyadventure_sundaycount"));
        String[] dayBuffStr = DataManager.getCommConfig("familyadventure_daybuff").split("\\|");
        Map<Integer, FTBuffVo> buffVoMap = new HashMap<>();
        for (String dayBuffs : dayBuffStr) {
            String[] buffs = dayBuffs.split("\\+");
            FTBuffVo vo = new FTBuffVo();
            vo.setDay(Integer.parseInt(buffs[0]));
            vo.setBuffid(Integer.parseInt(buffs[1]));
            vo.setLevel(Integer.parseInt(buffs[2]));
            buffVoMap.put(vo.getDay(), vo);
        }
        FamilyTreasureManager.buffVoMap = buffVoMap;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(FamilyTreasureStageEvent.class, new FamilyTreasureListener((FamilyTreasureModule) module));
        eventDispatcher.reg(LeaveOrKickOutFamilyEvent.class, new LeaveOrKickOutFamilyListener((FamilyTreasureModule) module));
    }
}
