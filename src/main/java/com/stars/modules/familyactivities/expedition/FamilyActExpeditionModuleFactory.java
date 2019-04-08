package com.stars.modules.familyactivities.expedition;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.expedition.gm.FamilyActExpeditionGmHandler;
import com.stars.modules.familyactivities.expedition.prodata.FamilyActExpeditionBuffInfoVo;
import com.stars.modules.familyactivities.expedition.prodata.FamilyExpeditionVo;
import com.stars.modules.gm.GmManager;
import com.stars.services.activities.ActConst;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
public class FamilyActExpeditionModuleFactory extends AbstractModuleFactory<FamilyActExpeditionModule> {

    public FamilyActExpeditionModuleFactory() {
        super(new FamilyActExpeditionPacketSet());
    }

    @Override
    public FamilyActExpeditionModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyActExpeditionModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        /* 初始化gm命令 */
        GmManager.reg("family.act.expedition", new FamilyActExpeditionGmHandler());
    }

    @Override
    public void loadProductData() throws Exception {
        /* familyexpedition */
        List<FamilyExpeditionVo> list = DBUtil.queryList(
                DBUtil.DB_PRODUCT, FamilyExpeditionVo.class,
                "select * from `familyexpedition`");
        Map<Integer, Map<Integer, FamilyExpeditionVo>> expeditionVoMap = new HashMap<>();
        Map<Integer, Integer> familyLevel2ExpeditionIdMap = new HashMap<>();
        for (FamilyExpeditionVo vo : list) {
            Map<Integer, FamilyExpeditionVo> submap = expeditionVoMap.get(vo.getExpeditionId());
            if (submap == null) {
                submap = new HashMap<>();
                expeditionVoMap.put(vo.getExpeditionId(), submap);
            }
            submap.put(vo.getStep(), vo);
            if (!familyLevel2ExpeditionIdMap.containsKey(vo.getReqFamilyLevel())
                    || familyLevel2ExpeditionIdMap.get(vo.getReqFamilyLevel()) < vo.getExpeditionId()) {
                familyLevel2ExpeditionIdMap.put(vo.getReqFamilyLevel(), vo.getExpeditionId());
            }
        }
        /* commondefine */
        String buffInfoStr = DataManager.getCommConfig("family_expeditionbuff");
        Map<Integer, FamilyActExpeditionBuffInfoVo> buffInfoVoMap = new HashMap<>();
        Map<Integer, Integer> buffLevelMap = new HashMap<>();
        for (String s : StringUtil.toArray(buffInfoStr, String[].class, '|')) {
            String[] array = StringUtil.toArray(s, String[].class, '+');
            FamilyActExpeditionBuffInfoVo vo = new FamilyActExpeditionBuffInfoVo(
                    Integer.parseInt(array[0]), Integer.parseInt(array[2]), Integer.parseInt(array[3]));
            buffInfoVoMap.put(vo.getId(), vo);
            buffLevelMap.put(vo.getId(), 1);
        }


        /* assign value*/
        FamilyActExpeditionManager.expeditionVoMap = expeditionVoMap;
        FamilyActExpeditionManager.familyLevel2ExpeditionIdMap = familyLevel2ExpeditionIdMap;
        FamilyActExpeditionManager.buffInfoVoMap = buffInfoVoMap;
        FamilyActExpeditionManager.buffLevelMap = buffLevelMap;

        int maxId = 0;
        for (Integer expeId : expeditionVoMap.keySet()) {
            if (expeId > maxId) {
                maxId = expeId;
            }
        }
        FamilyActExpeditionManager.maxId = maxId;

        /* 初始化活动流程 */
        FamilyActExpeditionFlow flow = new FamilyActExpeditionFlow();
        flow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_EXPEDITION));
        FamilyActExpeditionManager.flow = flow;
    }
}
