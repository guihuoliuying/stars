package com.stars.modules.rank;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.gamecave.event.FinishAllTinyGameEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.rank.gm.RankGmHandler;
import com.stars.modules.rank.listener.RoleRenameListenner;
import com.stars.modules.rank.listener.UpdateRankListener;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.skytower.event.FinishSkyTowerLayerEvent;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.prodata.RankDisplayVo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class RankModuleFactory extends AbstractModuleFactory<RankModule> {
    public RankModuleFactory() {
        super(new RankPacketSet());
    }

    @Override
    public RankModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new RankModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("rank",new RankGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        UpdateRankListener listener = new UpdateRankListener((RankModule) module);
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(FinishAllTinyGameEvent.class, listener);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(FinishSkyTowerLayerEvent.class, listener);
        eventDispatcher.reg(RoleRenameEvent.class, new RoleRenameListenner((RankModule)module));
    }

    @Override
    public void loadProductData() throws Exception {
        loadRankDisplayVo();
        loadRankRewardVo();
    }

    private void loadRankDisplayVo() throws SQLException {
        // rankvo
        Map<Integer, RankDisplayVo> rankDisplayVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "rankid", RankDisplayVo.class,
                "select * from `rankdisplay`; ");
        RankManager.rankDisplayVoMap = rankDisplayVoMap;
    }

    private void loadRankRewardVo() throws SQLException {
        // rewardvo
        Map<Integer, List<RankAwardVo>> rewardVoMap = new HashMap<>();
        Map<Byte, List<Integer>> rewardTypeMap = new HashMap<>();
        rewardTypeMap.put(RankConstant.REWARD_TYPE_OTHER, new LinkedList<Integer>());
        rewardTypeMap.put(RankConstant.REWARD_TYPE_DAILY, new LinkedList<Integer>());
        rewardTypeMap.put(RankConstant.REWARD_TYPE_WEEKLY, new LinkedList<Integer>());
        rewardTypeMap.put(RankConstant.REWARD_TYPE_APPOINT, new LinkedList<Integer>());
        List<RankAwardVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, RankAwardVo.class, "select * from `rankaward`; ");
        for (RankAwardVo vo : list) {
            List<RankAwardVo> rewardList = rewardVoMap.get(vo.getRankId());
            if (rewardList == null) {
                rewardList = new LinkedList<>();
                rewardVoMap.put(vo.getRankId(), rewardList);
            }
            if (!rewardTypeMap.get(vo.getRewardType()).contains(vo.getRankId()))
                rewardTypeMap.get(vo.getRewardType()).add(vo.getRankId());
            rewardList.add(vo);
        }
        RankManager.rankRewardVoMap = rewardVoMap;
        RankManager.rewardTypeMap = rewardTypeMap;
    }
}
