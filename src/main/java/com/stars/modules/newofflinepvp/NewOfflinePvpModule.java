package com.stars.modules.newofflinepvp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.newofflinepvp.packet.ClientNewOfflinePvp;
import com.stars.modules.newofflinepvp.prodata.OfflineAwardVo;
import com.stars.modules.newofflinepvp.userdata.RoleOfflinePvpPo;
import com.stars.modules.operateCheck.OperateCheckModule;
import com.stars.modules.operateCheck.OperateConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.skyrank.event.SkyRankScoreHandleEvent;
import com.stars.modules.skyrank.prodata.SkyRankScoreVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.services.newofflinepvp.userdata.NewOfflinePvpRankPo;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2017-03-08 15:34
 */
public class NewOfflinePvpModule extends AbstractModule {
    private RoleOfflinePvpPo roleOfflinePvpPo;
    private long otherId;
    private List<Long> fightIds;

    public NewOfflinePvpModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleOfflinePvpPo = DBUtil.queryBean(DBUtil.DB_USER, RoleOfflinePvpPo.class, "select * from rolenewofflinepvp where roleid=" + id());
        insertRoleOfflinePvp();
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        insertRoleOfflinePvp();
    }

    private void insertRoleOfflinePvp() {
        if (roleOfflinePvpPo == null) {
            roleOfflinePvpPo = new RoleOfflinePvpPo();
            roleOfflinePvpPo.setRoleid(id());
            roleOfflinePvpPo.setPvpCount(0);
            roleOfflinePvpPo.setExtraCount(0);
            roleOfflinePvpPo.setBuyCount(0);
            roleOfflinePvpPo.setMaxRank(-1);
            roleOfflinePvpPo.setLastMaxRank(-1);
            context().insert(roleOfflinePvpPo);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleOfflinePvpPo != null) {
            roleOfflinePvpPo.setPvpCount(0);
            roleOfflinePvpPo.setBuyCount(0);
            roleOfflinePvpPo.setExtraCount(0);
            context().update(roleOfflinePvpPo);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ForeShowModule open = module(MConst.ForeShow);
        if (open.isOpen(ForeShowConst.OfflinePvp)) {
            openOfflinePvp();
        }
    }

    @Override
    public void onOffline() throws Throwable {
        SceneModule scene = module(MConst.Scene);
        if (scene.getLastSceneType() == SceneManager.SCENETYPE_NEWOFFLINEPVP) {
            ServiceHelper.newOfflinePvpService().unLockRole(id(), otherId);
        }
    }

    @Override
    public void onTimingExecute() {

    }

    /**
     * 同步最新的排名
     *
     * @param newRank
     */
    public void doRankChangeEvent(int newRank) {
        int oldMaxRank = roleOfflinePvpPo.getMaxRank();
        if (newRank < oldMaxRank || oldMaxRank == -1) {
            roleOfflinePvpPo.setMaxRank(newRank);
            roleOfflinePvpPo.setLastMaxRank(oldMaxRank);
            context().update(roleOfflinePvpPo);
            sectionRankAward(newRank, oldMaxRank);
            historyRankAward(newRank, oldMaxRank);
        }
    }

    /**
     * 历史排名发奖
     *
     * @param newRank
     * @param oldRank
     */
    private void historyRankAward(int newRank, int oldRank) {
        List<OfflineAwardVo> awardVos = NewOfflinePvpManager.getOfflineAwardVo(newRank, oldRank);
        DropModule drop = module(MConst.Drop);
        Map<Integer, Integer> itemMaps = new HashMap<>();
        if (awardVos != null) {
            for (OfflineAwardVo awardVo : awardVos) {
                combinehistoryRankAward(drop, itemMaps, awardVo);
            }
        }
        if (!itemMaps.isEmpty()) {
            ToolModule tool = module(MConst.Tool);
            tool.addAndSend(itemMaps, EventType.OFFLINEPVP.getCode());
            ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.historyRankAward);
            clientNewOfflinePvp.setMyMaxRank(newRank);
            clientNewOfflinePvp.setHistoryRankAwardItemMaps(itemMaps);
            send(clientNewOfflinePvp);
        }
    }

    public void doMatchEvent(List<Long> fightList) {
        this.fightIds = fightList;
    }

    /**
     * 晋升奖励
     *
     * @param newRank
     * @param oldRank
     */
    private void sectionRankAward(int newRank, int oldRank) {
        Set<Integer> rankSectionSet = NewOfflinePvpManager.getSectionRankDrop(newRank, oldRank);
        if (rankSectionSet != null) {
            DropModule drop = module(MConst.Drop);
            Map<Integer, Integer> itemMaps = new HashMap<>();
            for (Integer groupId : rankSectionSet) {
                combineSectionRankAward(drop, itemMaps, groupId);
            }
            if (!itemMaps.isEmpty()) {
                ToolModule tool = module(MConst.Tool);
                tool.addAndSend(itemMaps, EventType.OFFLINEPVP.getCode());
                ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.sectionRankAward);
                clientNewOfflinePvp.setMyMaxRank(newRank);
                clientNewOfflinePvp.setOnWhichRank(NewOfflinePvpManager.getGodOrLandOrPerson(newRank, oldRank));
                clientNewOfflinePvp.setSectionRankAwardItemMaps(itemMaps);
                send(clientNewOfflinePvp);
            }
        }
    }

    /**
     * 进入战斗
     *
     * @param fightId
     * @param roleOrRobot
     */
    public void enterFight(long fightId, byte roleOrRobot) {
        if (fightIds != null && !fightIds.contains(fightId)) {
            warn("错误的战斗对象");
            return;
        }
        this.otherId = fightId;
        if (NewOfflinePvpManager.getMaxFightCount() - roleOfflinePvpPo.getPvpCount() + roleOfflinePvpPo.getExtraCount() <= 0) {
            warn("family_tips_nocount");
            return;
        }
        int stageId = Integer.parseInt(DataManager.getCommConfig("offline_stageid"));
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (stageVo == null) {
            com.stars.util.LogUtil.error("没有离线竞技场场景数据:{}", stageId);
            return;
        }
        boolean canEnter = ServiceHelper.newOfflinePvpService().lockRole(id(), fightId);
        if (canEnter) {
            SceneModule scene = module(MConst.Scene);
            scene.enterScene(SceneManager.SCENETYPE_NEWOFFLINEPVP, stageId, stageId + "-" + roleOrRobot + "-" + fightId);
        }
    }

    /**
     * 记录战斗次数
     */
    public void dealFightCount() {
        if (NewOfflinePvpManager.getMaxFightCount() - roleOfflinePvpPo.getPvpCount() > 0) {
            roleOfflinePvpPo.setPvpCount(roleOfflinePvpPo.getPvpCount() + 1);
            ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_OFFLINE_PVP, 1));
        } else {
            roleOfflinePvpPo.setExtraCount(roleOfflinePvpPo.getExtraCount() - 1);
        }
        context().update(roleOfflinePvpPo);
    }

    public int getMaxBuyCount() {
        int maxCount = NewOfflinePvpManager.getMaxBuyCount();
        VipModule vipModule = module(MConst.Vip);
        VipinfoVo vipinfoVo = VipManager.getVipinfoVo(vipModule == null ? -1 : vipModule.getVipLevel());
        if (vipinfoVo != null) {
            maxCount += vipinfoVo.getOfflineCount();
        }
        return maxCount;
    }

    /**
     * 购买次数
     */
    public void buyCount() {
        if (roleOfflinePvpPo.getBuyCount() >= getMaxBuyCount()) {
            warn("bag_vigor_maxtime");
            return;
        }
        if (!OperateCheckModule.checkOperate(id(), OperateConst.BUY_PVP_TIMES, OperateConst.FIVE_HUNDRED_MS)) return;
        ToolModule tool = module(MConst.Tool);
        //扣除道具
        if (tool.deleteAndSend(NewOfflinePvpManager.getBuyCountItemId(), NewOfflinePvpManager.getBuyCountItemCount(), EventType.OFFLINEPVP.getCode())) {
            com.stars.util.LogUtil.info("扣除道具成功");
            roleOfflinePvpPo.setBuyCount(roleOfflinePvpPo.getBuyCount() + 1);
            roleOfflinePvpPo.setExtraCount(roleOfflinePvpPo.getExtraCount() + 1);
            context().update(roleOfflinePvpPo);
            ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.buyCount);
            clientNewOfflinePvp.setRemainBuyCount(getMaxBuyCount() - roleOfflinePvpPo.getBuyCount());
            clientNewOfflinePvp.setRemainCount(NewOfflinePvpManager.getMaxFightCount() - roleOfflinePvpPo.getPvpCount() + roleOfflinePvpPo.getExtraCount());
            send(clientNewOfflinePvp);
            warn("common_tips_buysuc");
        } else {
            String itemName = DataManager.getGametext(ToolManager.getItemName(NewOfflinePvpManager.getBuyCountItemId()));
            warn("bag_vigor_buy_nomoney", itemName);
        }
    }

    /**
     * 退出战斗
     *
     * @param fightId
     * @param finish
     */
    public void exitFight(long fightId, byte finish) {
        RoleModule roleModule = module(MConst.Role);
        Role role = roleModule.getRoleRow();
        if (finish == SceneManager.STAGE_VICTORY) {
            ServiceHelper.newOfflinePvpService().dealExitFight(NewOfflinePvpManager.victory, fightId, id(), role.getLevel(),
                    role.getJobId(), role.getName(), role.getFightScore());
        } else if (finish == SceneManager.STAGE_FAIL) {
            ServiceHelper.newOfflinePvpService().dealExitFight(NewOfflinePvpManager.defeat, fightId, id(), role.getLevel(),
                    role.getJobId(), role.getName(), role.getFightScore());
        } else {
            LogUtil.error("战斗结果有错误:{}", finish);
        }
    }

    /**
     * 退出战斗的包
     *
     * @param finish
     * @param myRank
     * @param updateRank
     */
    public void exitFightPacketToClient(byte finish, int myRank, int updateRank) {
        byte stageFinish = 0;
        try {
            if (finish == NewOfflinePvpManager.victory) {
                stageFinish = SceneManager.STAGE_VICTORY;
            } else {
                stageFinish = SceneManager.STAGE_FAIL;
            }
            DropModule drop = module(MConst.Drop);
            ToolModule tool = module(MConst.Tool);
            Map<Integer, Integer> itemMap = drop.executeDrop(NewOfflinePvpManager.getWinLoseAwardMap().get(finish), 1, true);
            tool.addAndSend(itemMap, EventType.OFFLINEPVP.getCode());
            ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_NEWOFFLINEPVP, stageFinish);
            clientStageFinish.setItemMap(itemMap);
            clientStageFinish.setMyRank(myRank);
            clientStageFinish.setUpdateRank(updateRank);
            send(clientStageFinish);
            // 天梯积分处理
        } finally {
            ServiceHelper.roleService().notice(id(), new SkyRankScoreHandleEvent(SkyRankScoreVo.TYPE_OFFLINEPVP, stageFinish));
        }
    }

    /**
     * 合并晋升奖励
     *
     * @param drop
     * @param itemMaps
     * @param groupId
     */
    private void combineSectionRankAward(DropModule drop, Map<Integer, Integer> itemMaps, Integer groupId) {
        Map<Integer, Integer> itemMap = drop.executeDrop(groupId, 1, true);
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            if (itemMaps.containsKey(entry.getKey())) {
                itemMaps.put(entry.getKey(), itemMaps.get(entry.getKey()) + entry.getValue());
            } else {
                itemMaps.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 合并排名奖励
     *
     * @param drop
     * @param itemMaps
     * @param awardVo
     */
    private void combinehistoryRankAward(DropModule drop, Map<Integer, Integer> itemMaps, OfflineAwardVo awardVo) {
        Map<Integer, Integer> itemMap = drop.executeDrop(awardVo.getAward(), 1, true);
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            if (itemMaps.containsKey(entry.getKey())) {
                itemMaps.put(entry.getKey(), itemMaps.get(entry.getKey()) + entry.getValue());
            } else {
                itemMaps.put(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * 请求打开界面
     */
    public void view() {
        int maxRank = roleOfflinePvpPo.getMaxRank();
        if (maxRank == 0 || maxRank == -1) {
            maxRank = 5001;
        }
        ServiceHelper.newOfflinePvpService().match(id(), maxRank,
                NewOfflinePvpManager.getMaxFightCount() - roleOfflinePvpPo.getPvpCount() + roleOfflinePvpPo.getExtraCount(),
                getMaxBuyCount() - roleOfflinePvpPo.getBuyCount(), roleOfflinePvpPo.getFirst());
        if (roleOfflinePvpPo.getFirst() == NewOfflinePvpManager.first) {
            roleOfflinePvpPo.setFirst((byte) 1);
            context().update(roleOfflinePvpPo);
        }
    }

    /**
     * 战力改变
     *
     * @param fightScore
     */
    public void changeFightScore(int fightScore) {
        ServiceHelper.newOfflinePvpService().changeRoleFightScore(id(), fightScore);
    }

    /**
     * 等级改变
     *
     * @param level
     */
    public void changeRoleLevel(int level) {
        ServiceHelper.newOfflinePvpService().changeRoleLevel(id(), level);
    }

    /**
     * 转职改变
     *
     * @param jobId
     */
    public void changeRoleJob(int jobId) {
        ServiceHelper.newOfflinePvpService().changeRoleJob(id(), jobId);
    }

    /**
     * 开启竞技场系统
     */
    public void openOfflinePvp() {
        RoleModule roleModule = module(MConst.Role);
        Role role = roleModule.getRoleRow();
        ServiceHelper.newOfflinePvpService().openOfflinePvp(role.getRoleId(),
                role.getLevel(), role.getJobId(), role.getName(), role.getFightScore());
    }

    /**
     * 请求排行榜数据
     */
    public void sendRankList() {
        ServiceHelper.newOfflinePvpService().sendRankList(id());
    }

    public void sendRankList(List<NewOfflinePvpRankPo> rankPoList, int onRank) {
        ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.rankAward);
        clientNewOfflinePvp.setRankAwardList(rankPoList);
        clientNewOfflinePvp.setOnRank(onRank);
        if (onRank == NewOfflinePvpManager.notOnRank) {
            RoleModule roleModule = module(MConst.Role);
            clientNewOfflinePvp.setRoleIdStr(String.valueOf(roleModule.getRoleRow().getRoleId()));
            clientNewOfflinePvp.setRoleNameStr(roleModule.getRoleRow().getName());
            clientNewOfflinePvp.setFightScore(roleModule.getFightScore());
        }
        send(clientNewOfflinePvp);
    }

    /**
     * 请求战报数据
     */
    public void sendBattleReport() {
        ServiceHelper.newOfflinePvpService().sendBattleReport(id());
    }

    public int getPvpCount() {
        if (roleOfflinePvpPo != null) {
            int count = roleOfflinePvpPo.getPvpCount() + roleOfflinePvpPo.getBuyCount() - roleOfflinePvpPo.getExtraCount();
            return count;
        }
        return -1;
    }

    public void getFightCount() {
        ClientNewOfflinePvp pvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.fightCount);
        pvp.setFightTimes(roleOfflinePvpPo == null ? 0 : NewOfflinePvpManager.getMaxFightCount() - roleOfflinePvpPo.getPvpCount() + roleOfflinePvpPo.getExtraCount());
        send(pvp);
    }

    /**
     * 角色改名
     *
     * @param newName
     */
    public void onRoleReName(String newName) {
        ServiceHelper.newOfflinePvpService().changeRoleName(id(), newName);
    }
}
