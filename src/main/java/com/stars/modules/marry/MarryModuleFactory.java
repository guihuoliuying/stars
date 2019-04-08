package com.stars.modules.marry;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.marry.event.MarrySceneFinishEvent;
import com.stars.modules.marry.event.SyncSelfDataToTeamEvent;
import com.stars.modules.marry.listener.*;
import com.stars.modules.marry.prodata.MarryActivityVo;
import com.stars.modules.marry.prodata.MarryBattleScoreVo;
import com.stars.modules.marry.prodata.MarryRing;
import com.stars.modules.marry.prodata.MarryRingLvl;
import com.stars.modules.marry.summary.MarrySummaryComponentImpl;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.services.marry.event.*;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryModuleFactory extends AbstractModuleFactory {

    public MarryModuleFactory() {
        super(new MarryPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<MarryActivityVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, MarryActivityVo.class, "select * from marryactivity");
        ConcurrentMap<Integer, MarryActivityVo> map = new ConcurrentHashMap<>();
        for (MarryActivityVo vo : list) {
            map.put(vo.getActivitytype(), vo);
        }

        String[] delay = DataManager.getCommConfig("marry_activity_cdtime").split("[+]");

        String[] attention = DataManager.getCommConfig("marry_party_beginattention").split("[+]");

        String[] profressLimit = DataManager.getCommConfig("marry_profress_limit").split("[+]");

        List<MarryRing> list1 = DBUtil.queryList(DBUtil.DB_PRODUCT, MarryRing.class, "select * from marryring");
        ConcurrentMap<Integer, MarryRing> map1 = new ConcurrentHashMap<>();
        for (MarryRing vo : list1) {
            map1.put(vo.getRingid(), vo);
        }

        List<MarryRingLvl> list2 = DBUtil.queryList(DBUtil.DB_PRODUCT, MarryRingLvl.class, "select * from marryringlvl");
        ConcurrentMap<String, MarryRingLvl> map2 = new ConcurrentHashMap<>();
        for (MarryRingLvl vo : list2) {
            map2.put(vo.getRingid() + "_" + vo.getLevel(), vo);
        }

        MarryManager.marryBattleScoreVoMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "monsterid", MarryBattleScoreVo.class, "select * from marrybattlescore");

        MarryManager.REDBAG_DELAY = Integer.valueOf(delay[0]);
        MarryManager.FIREWORKS_DELAY = Integer.valueOf(delay[1]);
        MarryManager.CANDY_DELAY = Integer.valueOf(delay[2]);
        MarryManager.BEGINATTENTION = Integer.valueOf(attention[0]);
        MarryManager.ATTENTION_INTERVAL = Integer.valueOf(attention[1]);
        MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC = Integer.valueOf(DataManager.getCommConfig("marry_loveinfo_holdtime_public"));
        MarryManager.MAX_PROFRESS_LIMIT = Integer.valueOf(profressLimit[0]);
        MarryManager.CONFIG_PROFRESS_OUTTIME = (Integer.valueOf(profressLimit[1])) * 3600;

        MarryManager.activityMap = map;
        MarryManager.ringMap = map1;
        MarryManager.ringLvMap = map2;
        MarryManager.TEAM_DUNGEON_COUNT = Integer.parseInt(DataManager.getCommConfig("marry_battlestage_times"));
    }

    @Override
    public void init() throws Exception {
        MarryManager.registEvent();
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_MARRY, MarryTeamHandler.class);
        Summary.regComponentClass(SummaryConst.C_MARRY, MarrySummaryComponentImpl.class);
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new MarryModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(MarryToolEvent.class, new MarryToolEventListener(module));
//        eventDispatcher.reg(EnterWeddingSceneEvent.class, new EnterWeddingSceneListener(module));
        eventDispatcher.reg(LoginSuccessEvent.class, new LoginMarryListener(module));
        eventDispatcher.reg(MarryProfressEvent.class, new ProfressEventListener(module));
        eventDispatcher.reg(MarryEvent.class, new MarryEventListener(module));
        eventDispatcher.reg(EnterSceneEvent.class, new EnterSceneListener(module));
        eventDispatcher.reg(MarryAppointSceneCheckEvent.class, new MarryAppointSceneCheckEventListener(module));
        eventDispatcher.reg(WeddingActCheckEvent.class, new WeddingActCheckEventListener(module));
        eventDispatcher.reg(MarryLogEvent.class, new MarryLogListener(module));
        eventDispatcher.reg(RoleRenameEvent.class, new RoleChangeListenner((MarryModule) module));
        eventDispatcher.reg(SyncSelfDataToTeamEvent.class, new SyncSelfDataToTeamListener(module));
        eventDispatcher.reg(MarrySceneFinishEvent.class, new MarrySceneFinishListener(module));
    }
}
