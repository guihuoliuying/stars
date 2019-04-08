package com.stars.modules.daregod;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.daregod.event.DareGodEnterFightEvent;
import com.stars.modules.daregod.event.DareGodGetAwardEvent;
import com.stars.modules.daregod.listener.DareGodListener;
import com.stars.modules.daregod.prodata.SsbBoss;
import com.stars.modules.daregod.prodata.SsbBossTarget;
import com.stars.modules.daregod.prodata.SsbRankAward;
import com.stars.modules.daregod.prodata.VipBuyTimeForDareGod;
import com.stars.modules.data.DataManager;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;

import java.util.*;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodModuleFactory extends AbstractModuleFactory {
    public DareGodModuleFactory() {
        super(new DareGodPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, SsbBossTarget> ssbBossTargetMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", SsbBossTarget.class, "select * from ssbbosstarget");
        DareGodManager.setSsbBossTargetMap(ssbBossTargetMap);
        List<SsbRankAward> ssbRankAwardList = DBUtil.queryList(DBUtil.DB_PRODUCT, SsbRankAward.class, "select * from ssbrankaward");
        List<SsbBoss> ssbBossList = DBUtil.queryList(DBUtil.DB_PRODUCT, SsbBoss.class, "select * from ssbboss");
        Map<Integer, List<SsbBoss>> ssbBossMap = new HashMap<>();
        Map<Integer, String> fightTypeDesc = new HashMap<>();
        Map<Integer, String> fightTypeShape = new HashMap<>();
        Set<Integer> fightTypeSet = new HashSet<>();
        for (SsbBoss ssbBoss : ssbBossList) {
            List<SsbBoss> tmpList = ssbBossMap.get(ssbBoss.getPlat());
            if (tmpList == null) {
                ssbBossMap.put(ssbBoss.getPlat(), tmpList = new ArrayList<>());
            }
            tmpList.add(ssbBoss);
            fightTypeDesc.put(ssbBoss.getFightingType(), ssbBoss.getSectionName());
            fightTypeShape.put(ssbBoss.getFightingType(), ssbBoss.getShape());
            fightTypeSet.add(ssbBoss.getFightingType());
        }
        DareGodManager.setFightTypeDescMap(fightTypeDesc);
        DareGodManager.setFightTypeShapeMap(fightTypeShape);
        DareGodManager.setSsbBossMap(ssbBossMap);
        DareGodManager.setFightTypeSet(fightTypeSet);
        Map<Integer, List<SsbRankAward>> rankAwardMap = new HashMap<>();
        for (SsbRankAward ssbRankAward : ssbRankAwardList) {
            List<SsbRankAward> tmpList = rankAwardMap.get(ssbRankAward.getFightingType());
            if (tmpList == null) {
                rankAwardMap.put(ssbRankAward.getFightingType(), tmpList = new ArrayList<>());
            }
            tmpList.add(ssbRankAward);
        }
        DareGodManager.setRankAwardMap(rankAwardMap);

        DareGodManager.DARE_FREE_TIMES = DataManager.getCommConfig("ssb_daily_challenge_times", 3);
        DareGodManager.BUY_TIMES_REQ_ITEM_COUNT = DataManager.getCommConfig("ssb_challenge_time_cost", 20);
        String tmpStr = DataManager.getCommConfig("ssb_vipbugnub");
        String[] tmp0 = tmpStr.split("\\|");
        List<VipBuyTimeForDareGod> dareGods = new ArrayList<>();
        for (String tmp1 : tmp0) {
            String[] tmp2 = tmp1.split(",");
            VipBuyTimeForDareGod time = new VipBuyTimeForDareGod();
            time.setMinVipLv(Integer.parseInt(tmp2[0]));
            time.setMaxVipLv(Integer.parseInt(tmp2[1]));
            time.setTimes(Integer.parseInt(tmp2[2]));
            dareGods.add(time);
        }
        DareGodManager.setDareGods(dareGods);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new DareGodModule(MConst.DareGod, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        DareGodListener listener = new DareGodListener(module);
        eventDispatcher.reg(DareGodGetAwardEvent.class, listener);
        eventDispatcher.reg(DareGodEnterFightEvent.class, listener);
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(RoleRenameEvent.class, listener);
        eventDispatcher.reg(FashionChangeEvent.class, listener);
        eventDispatcher.reg(ChangeJobEvent.class, listener);
    }
}
