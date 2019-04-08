package com.stars.services.mooncake;

import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.mooncake.MoonCakeConst;
import com.stars.modules.mooncake.packet.ClientMoonCake;
import com.stars.modules.mooncake.userdata.RoleMoonCakePo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.rank.RankManager;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-09-21.
 */
public class MoonCakeServiceActor extends ServiceActor implements MoonCakeService {
    private List<RoleMoonCakeCache> roleMoonCakeCacheList;
    private boolean isOpen;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.MoonCakeService, this);
        roleMoonCakeCacheList = new LinkedList<>();
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_MoonCake);
        if (curActId != -1 && !isOpen) {
            initMoonCake();
        }
    }

    @Override
    public void initMoonCake() {
        isOpen = true;
        loadUserData();
        LogUtil.info("接月饼活动开始啦");
    }

    @Override
    public void closeMoonCake() {
        isOpen = false;
        LogUtil.info("接月饼活动结束了|rankSize:{}", roleMoonCakeCacheList.size());
        Collections.sort(roleMoonCakeCacheList);
        for (int i = 0; i < roleMoonCakeCacheList.size(); i++) {
            RoleMoonCakeCache cakeCache = roleMoonCakeCacheList.get(i);
            RankAwardVo rankAwardVo = RankManager.getRankAwardVo(MoonCakeConst.RANKAWARDID, i + 1);
            if (rankAwardVo == null) continue;
            LogUtil.info("接月饼活动结束后发奖|roleId:{},rank:{},itemMap:{}", cakeCache.getRoleId(), i + 1, new HashMap<>(rankAwardVo.getRewardMap()));
            ServiceHelper.emailService().sendToSingle(cakeCache.getRoleId(), rankAwardVo.getEmail(), 0L, "系统", new HashMap<>(rankAwardVo.getRewardMap()), Integer.toString(i + 1));
        }
        try {
            DBUtil.execSql(DBUtil.DB_USER," delete from rolemooncake");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewRank(long roleId) {
        if (!isOpen) {
            PacketManager.send(roleId, new ClientText("活动未开始"));
            return;
        }
        Collections.sort(roleMoonCakeCacheList);
        for (int i = 0; i < roleMoonCakeCacheList.size(); i++) {
            RoleMoonCakeCache roleMoonCakeCache = roleMoonCakeCacheList.get(i);
            RoleSummaryComponent roleSummary = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleMoonCakeCache.getRoleId(), SummaryConst.C_ROLE);
            roleMoonCakeCache.setRoleName(roleSummary.getRoleName());
            roleMoonCakeCache.setFightScore(roleSummary.getFightScore());
            roleMoonCakeCache.setRank(i + 1);
            RankAwardVo rankAwardVo = RankManager.getRankAwardVo(MoonCakeConst.RANKAWARDID, i + 1);
            if (rankAwardVo == null) continue;
            roleMoonCakeCache.setAwardMap(new HashMap<>(rankAwardVo.getRewardMap()));
        }
        ClientMoonCake clientMoonCake = new ClientMoonCake(ClientMoonCake.RES_RANK);
        clientMoonCake.setCakeCacheList(new LinkedList<>(roleMoonCakeCacheList));
        PacketManager.send(roleId, clientMoonCake);
    }

    @Override
    public void view(long roleId, ClientMoonCake packet) {
        RoleMoonCakeCache cakeCache = new RoleMoonCakeCache(roleId);
        int rank = 51;
        LogUtil.info("view|roleId:{},rankSize:{}", roleId, roleMoonCakeCacheList.size());
        if (roleMoonCakeCacheList.contains(cakeCache)) {
            rank = roleMoonCakeCacheList.indexOf(cakeCache) + 1;
        }
        packet.setMoonCakeRank(rank);
        PacketManager.send(roleId, packet);
    }

    @Override
    public void updateMaxWeeklyScore(long roleId, int score/*, String roleName, int fightScore*/) {
        if (!isOpen) {
            LogUtil.info("updateMaxWeeklyScore|活动尚未开始|roleId:{},score:{}", roleId, score);
            return;
        }
        RoleMoonCakeCache cache = new RoleMoonCakeCache(roleId);
        if (!roleMoonCakeCacheList.contains(cache)) {
            RoleMoonCakeCache tmpCache = new RoleMoonCakeCache(roleId, score/*,roleName,fightScore*/);
            roleMoonCakeCacheList.add(tmpCache);
        } else {
            RoleMoonCakeCache roleMoonCakeCache = roleMoonCakeCacheList.get(roleMoonCakeCacheList.indexOf(cache));
            roleMoonCakeCache.setWeeklyMaxScore(score);
        }
        Collections.sort(roleMoonCakeCacheList);
        if (roleMoonCakeCacheList.size() > MoonCakeConst.MAX_RANK) {
            roleMoonCakeCacheList = roleMoonCakeCacheList.subList(0, MoonCakeConst.MAX_RANK);
        }
        LogUtil.info("updateMaxWeeklyScore|rankSize:{}", roleMoonCakeCacheList.size());
    }

    @Override
    public void removeFromRank(long roleId) {
        RoleMoonCakeCache cache = new RoleMoonCakeCache(roleId);
        if (!roleMoonCakeCacheList.contains(cache)) return;
        LogUtil.info("删除排行榜|roleId:{},rankSize:{}", roleId, roleMoonCakeCacheList.size());
        roleMoonCakeCacheList.remove(cache);
        Collections.sort(roleMoonCakeCacheList);
    }

    @Override
    public void updateRoleName(long roleId, String roleName) {
//        RoleMoonCakeCache cache = new RoleMoonCakeCache(roleId);
//        if (!roleMoonCakeCacheList.contains(cache)) return;
//        RoleMoonCakeCache roleMoonCakeCache = roleMoonCakeCacheList.get(roleMoonCakeCacheList.indexOf(cache));
//        roleMoonCakeCache.setRoleName(roleName);
    }

    @Override
    public void updateFightScore(long roleId, int fightScore) {
//        RoleMoonCakeCache cache = new RoleMoonCakeCache(roleId);
//        if (!roleMoonCakeCacheList.contains(cache)) return;
//        RoleMoonCakeCache roleMoonCakeCache = roleMoonCakeCacheList.get(roleMoonCakeCacheList.indexOf(cache));
//        roleMoonCakeCache.setFightScore(fightScore);
    }

    @Override
    public void printState() {

    }

    private void loadUserData() {
        try {
            List<RoleMoonCakeCache> tmpList = new LinkedList<>();
            List<RoleMoonCakePo> roleMoonCakePos = DBUtil.queryList(DBUtil.DB_USER, RoleMoonCakePo.class, "select * from rolemooncake");
            for (RoleMoonCakePo roleMoonCakePo : roleMoonCakePos) {
                if (roleMoonCakePo.getiWeekSingleMaxScore() <= 0) continue;
//                Summary summary = ServiceHelper.summaryService().getSummary(roleMoonCakePo.getRoleId());
//                RoleSummaryComponent roleSummary = (RoleSummaryComponent) summary;
                RoleMoonCakeCache roleMoonCakeCache = new RoleMoonCakeCache(roleMoonCakePo.getRoleId(), roleMoonCakePo.getiWeekSingleMaxScore()/*, roleSummary.getRoleName(), roleSummary.getFightScore()*/);
                tmpList.add(roleMoonCakeCache);
            }
            Collections.sort(tmpList);
            if (tmpList.size() > MoonCakeConst.MAX_RANK) {
                roleMoonCakeCacheList = tmpList.subList(0, MoonCakeConst.MAX_RANK);
            } else {
                roleMoonCakeCacheList = tmpList;
            }
            LogUtil.info("loadUserData|rankSize:{}", roleMoonCakeCacheList.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
