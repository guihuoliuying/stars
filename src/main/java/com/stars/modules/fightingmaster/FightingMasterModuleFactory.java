package com.stars.modules.fightingmaster;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.fightingmaster.event.FiveRewardStatusEvent;
import com.stars.modules.fightingmaster.event.GetFiveRewardEvent;
import com.stars.modules.fightingmaster.gm.FightingMasterGM;
import com.stars.modules.fightingmaster.listener.EnterFightingMasterEventListener;
import com.stars.modules.fightingmaster.listener.FightingMasterEventListener;
import com.stars.modules.fightingmaster.listener.RoleRenameListenner;
import com.stars.modules.fightingmaster.prodata.PersonPKcoeVo;
import com.stars.modules.fightingmaster.prodata.PersonPaircoeVo;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.multiserver.fightingmaster.Matcher;
import com.stars.services.fightingmaster.event.EnterFightingMasterEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class FightingMasterModuleFactory extends AbstractModuleFactory {

    public FightingMasterModuleFactory() {
        super(new FightingMasterPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<PersonPKcoeVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, PersonPKcoeVo.class, "select * from personpkcoe");
        Matcher<PersonPKcoeVo> matcher = new Matcher<>();
        matcher.add(list);

        // 设置边界情况匹配
        Map<String, PersonPKcoeVo> map = new HashMap<>();
        PersonPKcoeVo maxVo = null;
        int tempMax = 0;
        int maxDisSegmentRankId = 0;
        for (PersonPKcoeVo vo : list) {
            PersonPKcoeVo max = map.get(vo.getPowersection());
            if (max == null || max.compare(vo) < 0) {
                map.put(vo.getPowersection(), vo);
            }
            if (maxVo == null || maxVo.compare(vo) < 0) {
                maxVo = vo;
            }
            if (vo.getMinScore() > tempMax) {
                tempMax = vo.getMinScore();
                maxDisSegmentRankId = vo.getRankid();
            }
        }

        FightingMasterManager.maxDisSegmentRankId = maxDisSegmentRankId;
        FightingMasterManager.maxDisScore = tempMax;

        for (PersonPKcoeVo vo : map.values()) {
            PersonPKcoeVo copy = vo.copy();
            copy.setCminScore(vo.getCmaxScore() + 1);
            copy.setCmaxScore(Integer.MAX_VALUE);
            matcher.add(copy);
        }
        PersonPKcoeVo maxcopy = maxVo.copy();
        maxcopy.setCminPower(maxVo.getCmaxPower() + 1);
        maxcopy.setCmaxPower(Integer.MAX_VALUE);
        maxcopy.setCminScore(1);
        maxcopy.setCmaxScore(Integer.MAX_VALUE);
        matcher.add(maxcopy);

        FightingMasterManager.pkVo = matcher;

        List<PersonPaircoeVo> plist = DBUtil.queryList(DBUtil.DB_PRODUCT, PersonPaircoeVo.class, "select * from personpaircoe");
        Matcher<PersonPaircoeVo> pMatcher = new Matcher<>();
        pMatcher.add(plist);
        PersonPaircoeVo maxPair = null;
        for (PersonPaircoeVo vo : plist) {
            if (maxPair == null || maxPair.compare(vo) < 0) {
                maxPair = vo;
            }
        }
        maxPair = maxPair.copy();
        maxPair.setCminScore(maxPair.getMaxScore() + 1);
        maxPair.setCmaxScore(Integer.MAX_VALUE);
        pMatcher.add(maxPair);
        FightingMasterManager.pairVo = pMatcher;

        FightingMasterManager.stageId = DataManager.getCommConfig("personpk_stageid", 999);
        FightingMasterManager.fighttime = DataManager.getCommConfig("personpk_fighttime", 60);
        FightingMasterManager.awardCount = DataManager.getCommConfig("personpk_awardcount", 0);
        String[] pairmatch = DataManager.getCommConfig("personpk_pairmatchcount", "4+50").split("[+]");
        FightingMasterManager.pairmatchcount = Integer.valueOf(pairmatch[0]);
        FightingMasterManager.pairmatchPersent = Integer.valueOf(pairmatch[1]);
        String[] protectscore = DataManager.getCommConfig("personpk_protectscore", "4+-4").split("[+]");
        FightingMasterManager.winProtectScore = Integer.valueOf(protectscore[0]);
        FightingMasterManager.loseProtectScore = Integer.valueOf(protectscore[1]);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("fm", new FightingMasterGM());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(EnterFightingMasterEvent.class, new EnterFightingMasterEventListener(module));
        eventDispatcher.reg(FiveRewardStatusEvent.class, new FightingMasterEventListener(module));
        eventDispatcher.reg(GetFiveRewardEvent.class, new FightingMasterEventListener(module));
        eventDispatcher.reg(RoleRenameEvent.class, new RoleRenameListenner((FightingMasterModule) module));
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new FightingMasterModule(id, self, eventDispatcher, map);
    }
}
