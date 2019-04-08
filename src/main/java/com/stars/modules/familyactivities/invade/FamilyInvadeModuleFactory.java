package com.stars.modules.familyactivities.invade;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.buddy.event.FightBuddyChangeEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.invade.event.*;
import com.stars.modules.familyactivities.invade.gm.FamilyActInvadeGmHandler;
import com.stars.modules.familyactivities.invade.listener.FamilyInvadeListener;
import com.stars.modules.familyactivities.invade.prodata.FamilyInvadeVo;
import com.stars.modules.gm.GmManager;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyInvadeModuleFactory extends AbstractModuleFactory<FamilyInvadeModule> {
    public FamilyInvadeModuleFactory() {
        super(new FamilyInvadePacket());
    }

    @Override
    public FamilyInvadeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyInvadeModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommondefine();
        loadInvadeVo();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        FamilyInvadeListener listener = new FamilyInvadeListener((FamilyInvadeModule) module);
        eventDispatcher.reg(FamilyInvadeDungeonDropEvent.class, listener);
        eventDispatcher.reg(FamilyInvadeDungeonFinishEvent.class, listener);
        eventDispatcher.reg(FamilyInvadeAwardBoxEvent.class, listener);
        eventDispatcher.reg(FamilyActInvadeStartEvent.class, listener);
        eventDispatcher.reg(FamilyInvadeEnterDungeonEvent.class, listener);
        // 数据更新
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(SkillPositionChangeEvent.class, listener);
        eventDispatcher.reg(FightBuddyChangeEvent.class, listener);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("family.act.invade", new FamilyActInvadeGmHandler());
    }

    private void loadInvadeVo() throws SQLException {
        String sql = "select * from `familyinvade`; ";
        FamilyInvadeManager.invadeVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "invadeid", FamilyInvadeVo.class, sql);
    }

    private void loadCommondefine() throws Exception {
        String temp = DataManager.getCommConfig("familyinvade_monsterposition");
        int[] array = StringUtil.toArray(temp, int[].class, '+');
        if (array[0] > array[1] || array[3] > array[4]) {
            throw new IllegalArgumentException("commondefine表familyinvade_monsterposition字段配置错误");
        }
        FamilyInvadeManager.monsterNpcPosX = new int[]{array[0], array[1]};
        FamilyInvadeManager.monsterNpcPosY = array[2];
        FamilyInvadeManager.monsterNpcPosZ = new int[]{array[3], array[4]};
        array = StringUtil.toArray(DataManager.getCommConfig("familyinvade_flashcoefficient"), int[].class, '+');
        FamilyInvadeManager.normalMonsterSpawnCoef = array[0];
        FamilyInvadeManager.eliteMonsterSpawnCoef = array[1];
        array = StringUtil.toArray(DataManager.getCommConfig("familyinvade_monsternpc"), int[].class, '+');
        FamilyInvadeManager.normalMonsterNpcId = array[0];
        FamilyInvadeManager.eliteMonsterNpcId = array[1];
        array = StringUtil.toArray(DataManager.getCommConfig("familyinvade_boxnpc"), int[].class, '+');
        FamilyInvadeManager.awardBoxNpcId = array[0];
        FamilyInvadeManager.awardBoxNum = array[1];
        Map<Integer, Integer> boxReward = new HashMap<>();
        boxReward.put(array[2], array[3]);
        FamilyInvadeManager.boxReward = boxReward;
        FamilyInvadeManager.awardBoxShow = Long.parseLong(DataManager.getCommConfig("familyinvade_boxlate")) * 1000;
        temp = DataManager.getCommConfig("familyinvade_rankaward");
        List<int[]> rankGruop = new LinkedList<>();
        int rewardRankMax = 0;
        Map<Integer, Map<Integer, Integer>> rankGroupAward = new HashMap<>();
        int rankGroup = 1;
        for (String rankAward : temp.split("\\|")) {
            String[] awardArray = rankAward.split(",");
            int[] rankAwardArray = StringUtil.toArray(awardArray[0], int[].class, '+');
            rankGruop.add(new int[]{rankAwardArray[0], rankAwardArray[1], rankGroup});
            Map<Integer, Integer> rewardMap = new HashMap<>();
            for (int i = 1; i < awardArray.length; i++) {
                MapUtil.add(rewardMap, StringUtil.toMap(awardArray[i], Integer.class, Integer.class, '+', ','));
            }
            if (rewardRankMax < rankAwardArray[1]) {
                rewardRankMax = rankAwardArray[1];
            }
            rankGroupAward.put(rankGroup, rewardMap);
            rankGroup++;
        }
        FamilyInvadeManager.rankGroup = rankGruop;
        FamilyInvadeManager.rankGroupAward = rankGroupAward;
        FamilyInvadeManager.rewardRankMax = rewardRankMax;
        byte[] arrayByte = StringUtil.toArray(DataManager.getCommConfig("familyinvade_teammember"), byte[].class, '+');
        FamilyInvadeManager.minTeamCount = arrayByte[0];
        FamilyInvadeManager.maxTeamCount = arrayByte[1];
    }
}
