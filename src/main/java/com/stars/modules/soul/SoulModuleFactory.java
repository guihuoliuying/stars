package com.stars.modules.soul;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.gm.GmManager;
import com.stars.modules.soul.gm.SoulGm;
import com.stars.modules.soul.listenner.SoulListenner;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.prodata.SoulStage;
import com.stars.modules.tool.event.AddToolEvent;

import java.util.*;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulModuleFactory extends AbstractModuleFactory<SoulModule> {
    public SoulModuleFactory() {
        super(new SoulPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<SoulLevel> soulLevels = DBUtil.queryList(DBUtil.DB_PRODUCT, SoulLevel.class, "select * from soulgodlevel;");
        Map<Integer, Map<Integer, SoulLevel>> soulTypeMap = new HashMap<>();
        Map<Integer, Map<Integer, Map<Integer, SoulLevel>>> soulStageLevelMap = new HashMap<>();
        for (SoulLevel soulLevel : soulLevels) {
            int stage = soulLevel.getSoulGodStage();
            int level = soulLevel.getSoulGodLevel();
            int type = soulLevel.getSoulGodType();
            Map<Integer, SoulLevel> soulLevelMap = soulTypeMap.get(soulLevel.getSoulGodType());
            if (soulLevelMap == null) {
                soulLevelMap = new HashMap<>();
                soulTypeMap.put(soulLevel.getSoulGodType(), soulLevelMap);
            }
            soulLevelMap.put(soulLevel.getSoulGodLevel(), soulLevel);
            /**
             * soulStageLevelMap
             */
            Map<Integer, Map<Integer, SoulLevel>> typeLevelMap = soulStageLevelMap.get(stage);
            if (typeLevelMap == null) {
                typeLevelMap = new HashMap<>();
                soulStageLevelMap.put(stage, typeLevelMap);
            }
            Map<Integer, SoulLevel> levelMap = typeLevelMap.get(type);
            if (levelMap == null) {
                levelMap = new HashMap<>();
                typeLevelMap.put(type, levelMap);
            }
            levelMap.put(level, soulLevel);
        }
        Map<Integer, SoulStage> soulStageMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "stage", SoulStage.class, "select * from soulgodstage;");
        List<Integer> stages = new ArrayList<>(soulStageMap.keySet());
        Collections.sort(stages);
        SoulManager.maxSoulStage = stages.get(stages.size() - 1);
        SoulManager.soulTypeMap = soulTypeMap;
        SoulManager.soulStageMap = soulStageMap;
        SoulManager.soulStageLevelMap = soulStageLevelMap;

    }

    @Override
    public void init() throws Exception {
        GmManager.reg("soul", new SoulGm());
    }

    @Override
    public SoulModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new SoulModule("元神系统", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        SoulListenner soulListenner=new SoulListenner((SoulModule) module);
        eventDispatcher.reg(AddToolEvent.class,soulListenner);
    }
}

