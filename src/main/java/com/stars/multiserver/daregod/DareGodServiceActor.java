package com.stars.multiserver.daregod;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.persist.DbRowDao;
import com.stars.core.db.DBUtil;
import com.stars.modules.daregod.DareGodManager;
import com.stars.modules.daregod.event.DareGodEnterFightEvent;
import com.stars.modules.daregod.event.DareGodGetAwardEvent;
import com.stars.modules.daregod.packet.ClientDareGod;
import com.stars.modules.daregod.prodata.SsbBoss;
import com.stars.modules.daregod.prodata.SsbBossTarget;
import com.stars.modules.daregod.prodata.SsbRankAward;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.multiserver.chat.ChatRpcHelper;
import com.stars.multiserver.daregod.userdata.RankRoleDareGodCache;
import com.stars.multiserver.daregod.userdata.RoleDareGod;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodServiceActor extends ServiceActor implements DareGodService {
    private Map<Long, RoleDareGod> roleDareGodMap;//roleId, rolePo
    private Map<Integer, LinkedList<RankRoleDareGodCache>> roleDareRankList;//fightType,list of rolePo
    private DbRowDao rowDao;
    private boolean canFight;
    private DareGodFlow dareGodFlow;

    private boolean isView;

    private Map<Integer, Boolean> subServerMap;
    private boolean isLoadUserData = false;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.DareGodService, this);
        rowDao = new DbRowDao(SConst.DareGodService, DBUtil.DB_COMMON);
        this.roleDareGodMap = new HashMap<>();
        this.roleDareRankList = new HashMap<>();
        subServerMap = new HashMap<>();
        fillRankListByFightType();
        dareGodFlow = new DareGodFlow();
        dareGodFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_DAREGOD));
        long startTime = DareGodConst.getTimeL(DareGodFlow.START, ActConst.ID_DAREGOD);
        long stopTime = DareGodConst.getTimeL(DareGodFlow.STOP, ActConst.ID_DAREGOD);
        long now = System.currentTimeMillis();
        if (now > startTime && now < stopTime) {
            this.canFight = true;
        } else {
            this.canFight = false;
        }
    }

    @Override
    public void printState() {

    }

    @Override
    public void onLine(int serverId, int mainServerId, long roleId, String roleName, int fashionId, int fightScore, int jobId) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            newRoleDareGod(mainServerId, roleId, roleName, fashionId, fightScore, jobId);
        } else {
            roleDareGod.setServerId(mainServerId);
            roleDareGod.setRoleName(roleName);
            roleDareGod.setFashionId(fashionId);
            roleDareGod.setFightScore(fightScore);
            roleDareGod.setJobId(jobId);
            rowDao.update(roleDareGod);
        }
    }

    private void newRoleDareGod(int mainServerId, long roleId, String roleName, int fashionId, int fightScore, int jobId) {
        RoleDareGod roleDareGod;
        roleDareGod = new RoleDareGod();
        roleDareGod.setRoleId(roleId);
        roleDareGod.setRoleName(roleName);
        roleDareGod.setFashionId(fashionId);
        roleDareGod.setBuyTimes(0);
        roleDareGod.setCanFightTimes(DareGodManager.DARE_FREE_TIMES);
        roleDareGod.setDamage(0L);
        roleDareGod.setServerId(mainServerId);
        roleDareGod.setFightScore(fightScore);
        roleDareGod.setJobId(jobId);
        roleDareGodMap.put(roleId, roleDareGod);
        rowDao.insert(roleDareGod);
    }

    @Override
    public void view(int serverId, int mainServerId, long roleId, String roleName, int fightScore, int jobId, int vipLv) {
//        testData();
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            newRoleDareGod(mainServerId, roleId, roleName, -1, fightScore, jobId);
            roleDareGod = roleDareGodMap.get(roleId);
        }
        int subServer = getSubServer(mainServerId);
        SsbBoss ssbBoss = null;
        if (roleDareGod.getFightType() == 0) {
            ssbBoss = DareGodManager.getSsbBossByFightScore(subServer, fightScore);
        } else {
            ssbBoss = DareGodManager.getSsbBossByType(subServer, roleDareGod.getFightType());
        }
        if (ssbBoss == null) {
            LogUtil.info("没有对应的SsbBoss数据| serverId:{},roleId:{},fightScore:{}", mainServerId, roleId, fightScore);
            return;
        }
        if (roleDareGod.getFightType() == 0) {
            roleDareGod.setFightType(ssbBoss.getFightingType());
            Set<Integer> damageSet = ssbBoss.getDamageTargetSet();
            for (int targetId : damageSet) {
                roleDareGod.updateDamageDropState(targetId, DareGodConst.CANT_GET);
            }
        }
        ClientDareGod clientDareGod = new ClientDareGod(ClientDareGod.VIEW);
        clientDareGod.setCanFightTimes(roleDareGod.getCanFightTimes());
        clientDareGod.setCanBuyTimes(DareGodManager.getCanBuyTimes(vipLv) - roleDareGod.getBuyTimes());
        clientDareGod.setReqItems(DareGodManager.BUY_TIMES_REQ_ITEM_COUNT);
        clientDareGod.setRoleFightType(roleDareGod.getFightType());
        clientDareGod.setTotalDamage(roleDareGod.getDamage());
        clientDareGod.setTargetDropMap(roleDareGod.getTargetDropMap());
        clientDareGod.setTargetDropGroupMap(ssbBoss.getTargetDropGroupMap());
        Map<Integer, LinkedList<RankRoleDareGodCache>> tmpCacheMap = new HashMap<>();
        for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : roleDareRankList.entrySet()) {
            tmpCacheMap.put(entry.getKey(), new LinkedList<RankRoleDareGodCache>());
            Collections.sort(entry.getValue());
            for (int i = 0; i < entry.getValue().size() && i < 3; i++) {
                RankRoleDareGodCache dareGodCache = entry.getValue().get(i);
//                RankRoleDareGodCache cache = new RankRoleDareGodCache(dareGod.getRoleId(), dareGod.getServerId(), dareGod.getRoleName(),
//                        i + 1, dareGod.getDamage(), dareGod.getFightScore(), dareGod.getFightType(), dareGod.getFashionId(), dareGod.getJobId());
                dareGodCache.setRank(i + 1);
                tmpCacheMap.get(entry.getKey()).add(dareGodCache);
            }
        }
        clientDareGod.setDareGodCacheMap(tmpCacheMap);
        clientDareGod.setSsbBossList(DareGodManager.ssbBossMap.get(subServer));
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientDareGod);
    }

    @Override
    public void updateFightState(int serverId, boolean state) {
        canFight = state;
        LogUtil.info("挑战女神|状态改变|canFight:{}", canFight);
    }

    @Override
    public void onDaliyReset(int serverId) {
        List<Long> tmpRoleIds = new ArrayList<>();
        for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : roleDareRankList.entrySet()) {
            Collections.sort(entry.getValue());
            for (int i = 0; i < entry.getValue().size(); i++) {
                RankRoleDareGodCache dareGodCache = entry.getValue().get(i);
                tmpRoleIds.add(dareGodCache.getRoleId());
                SsbRankAward ssbRankAward = DareGodManager.getSsbRankAward(entry.getKey(), i + 1);
                if (ssbRankAward == null) {
                    LogUtil.info("挑战女神排行榜每日发奖出现异常|找不到产品数据|roleId:{},fightType:{},rank:{}", dareGodCache.getRoleId(), entry.getKey(), i + 1);
                    continue;
                }
                Map<Integer, Integer> tmpMap = DropUtil.executeDrop(ssbRankAward.getAward(), 1);
                try {
                    LogUtil.info("挑战女神排行榜每日发奖|roleId:{},serverId:{},roleName:{},fightTypeName:{},rank:{},itemMap:{}", dareGodCache.getRoleId(),
                            dareGodCache.getServerId(), dareGodCache.getRoleName(), DareGodManager.getFightTypeDesc(entry.getKey()), i + 1, tmpMap);
                    ChatRpcHelper.getMultiCommonService().sendToSingle(dareGodCache.getServerId(), dareGodCache.getRoleId(),
                            DareGodConst.RANK_EMAIL_ID, 0L, "系统", tmpMap, DareGodManager.getFightTypeDesc(entry.getKey()), Integer.toString(i + 1));
                } catch (Exception e) {
                    LogUtil.info("挑战女神排行榜每日发奖出现异常|发邮件出现异常|roleId:{},serverId:{}", dareGodCache.getRoleId(), dareGodCache.getServerId());
                    e.printStackTrace();
                }
            }
        }
        LogUtil.info("tmpRoleIds:{}", tmpRoleIds);
        for (RoleDareGod roleDareGod : roleDareGodMap.values()) {
            Set<Integer> unGetAwardSet = roleDareGod.getUnGetAward();
            int fightType = roleDareGod.getFightType();
            sendAwardOutOfRank(tmpRoleIds, roleDareGod, fightType);
            roleDareGod.onResetDaily();
            rowDao.update(roleDareGod);
            if (unGetAwardSet.isEmpty() || fightType == 0) {
                continue;
            }
            SsbBoss ssbBoss = DareGodManager.getSsbBossByType(getSubServer(roleDareGod.getServerId()), fightType);
            if (ssbBoss == null) {
                LogUtil.info("挑战女神每日发奖出现异常|找不到产品数据|roleId:{} , serverId:{} , fightType:{}", roleDareGod.getRoleId(), roleDareGod.getServerId(), fightType);
                continue;
            }
            Set<Integer> dropIds = ssbBoss.getDropIds(unGetAwardSet);
            Map<Integer, Integer> tmpMap = DropUtil.executeDrop(dropIds);
            try {
                ChatRpcHelper.getMultiCommonService().sendToSingle(roleDareGod.getServerId(), roleDareGod.getRoleId(),
                        DareGodConst.UN_GETAWARD_EMAIL_ID, 0L, "系统", tmpMap);
            } catch (Exception e) {
                LogUtil.info("挑战女神每日发奖出现异常|发邮件出现异常|roleId:{},serverId:{}", roleDareGod.getRoleId(), roleDareGod.getServerId());
                e.printStackTrace();
            }
        }
        fillRankListByFightType();
    }

    private void sendAwardOutOfRank(List<Long> tmpRoleIds, RoleDareGod roleDareGod, int fightType) {
        if (tmpRoleIds.contains(roleDareGod.getRoleId()) || fightType == 0) return;
        SsbRankAward ssbRankAward = DareGodManager.getSsbRankAward(fightType, 0);
        if (ssbRankAward == null) {
            LogUtil.info("挑战女神排行榜50名外每日发奖出现异常|找不到产品数据|roleId:{},fightType:{},rank:{}", roleDareGod.getRoleId(), fightType, "50+");
            return;
        }
        Map<Integer, Integer> tmpRankMap = DropUtil.executeDrop(ssbRankAward.getAward(), 1);
        try {
            LogUtil.info("挑战女神排行榜50名外每日发奖|roleId:{},serverId:{},roleName:{},fightTypeName:{},rank:{},itemMap:{}", roleDareGod.getRoleId(),
                    roleDareGod.getServerId(), roleDareGod.getRoleName(), DareGodManager.getFightTypeDesc(fightType), "50+", tmpRankMap);
            ChatRpcHelper.getMultiCommonService().sendToSingle(roleDareGod.getServerId(), roleDareGod.getRoleId(),
                    DareGodConst.RANK_EMAIL_ID, 0L, "系统", tmpRankMap, DareGodManager.getFightTypeDesc(fightType), "50+");
        } catch (Exception e) {
            LogUtil.info("挑战女神排行榜50名外每日发奖出现异常|发邮件出现异常|roleId:{},serverId:{}", roleDareGod.getRoleId(), roleDareGod.getServerId());
            e.printStackTrace();
        }
    }

    private int getSubServer(int mainServerId) {
        return mainServerId / 1000;
    }

    @Override
    public void viewRank(int serverId, int mainServerId, long roleId) {
        Map<Integer, LinkedList<RankRoleDareGodCache>> tmpCacheMap = new HashMap<>();
        LogUtil.info("roleDareRankList:{}", roleDareRankList.keySet());
        for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : roleDareRankList.entrySet()) {
            tmpCacheMap.put(entry.getKey(), new LinkedList<RankRoleDareGodCache>());
            Collections.sort(entry.getValue());
            for (int i = 0; i < entry.getValue().size(); i++) {
                RankRoleDareGodCache dareGodCache = entry.getValue().get(i);
//                RankRoleDareGodCache cache = new RankRoleDareGodCache(dareGod.getRoleId(), dareGod.getServerId(), dareGod.getRoleName(),
//                        i + 1, dareGod.getDamage(), dareGod.getFightScore(), dareGod.getFightType(), dareGod.getFashionId(), dareGod.getJobId());
                dareGodCache.setRank(i + 1);
                dareGodCache.setRankAward(DareGodManager.getSsbRankAwardId(entry.getKey(), i + 1));
                tmpCacheMap.get(entry.getKey()).add(dareGodCache);
            }
        }
        ClientDareGod clientDareGod = new ClientDareGod(ClientDareGod.VIEW_RANK);
        clientDareGod.setDareGodCacheMap(tmpCacheMap);
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientDareGod);
    }

    @Override
    public void dealExitOrFinishScene(int serverId, int mainServerId, long roleId, ClientStageFinish csf, long damage, int fightType) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            LogUtil.info("挑战女神退出战斗异常|找不到玩家|roleId:{},mainServerId:{},MapSize:{}", roleId, mainServerId, roleDareGodMap.size());
            return;
        }
        SsbBoss ssbBoss = DareGodManager.getSsbBossByType(getSubServer(mainServerId), fightType);
        if (ssbBoss == null) {
            LogUtil.info("挑战女神退出战斗异常|找不到产品数据|roleId:{},mainServerId:{},fightType:{}", roleId, mainServerId, roleDareGod.getFightType());
            return;
        }
        roleDareGod.addDamage(damage);
        Map<Integer, Integer> itemMap = DropUtil.executeDrop(ssbBoss.getHurtAward(), 1);
        updateDamageDropMap(ssbBoss, roleDareGod);
        rowDao.update(roleDareGod);
        ChatRpcHelper.getRoleService().notice(mainServerId, roleId, new DareGodGetAwardEvent(itemMap));
        doRank(mainServerId, roleId, fightType, roleDareGod);
        if (csf == null) {
            return;
        }
        csf.setDamageForDareGod(roleDareGod.getDamage());
        csf.setItemMap(itemMap);
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, csf);
//        ChatRpcHelper.getRoleService().notice(mainServerId, roleId, new DareGodGetAwardEvent(itemMap));
    }

    private void doRank(int mainServerId, long roleId, int fightType, RoleDareGod roleDareGod) {
        LinkedList<RankRoleDareGodCache> dareGodCaches = roleDareRankList.get(fightType);
        RankRoleDareGodCache tmpDareGodCache = new RankRoleDareGodCache(roleId, mainServerId, roleDareGod.getRoleName(), roleDareGod.getDamage(), roleDareGod.getFightScore(), roleDareGod.getFightType
                (), roleDareGod.getFashionId(), roleDareGod.getJobId());
        if (dareGodCaches == null) {
            dareGodCaches = new LinkedList<>();
        }
        if (dareGodCaches.contains(tmpDareGodCache)) {
            dareGodCaches.remove(tmpDareGodCache);
        }
        if (roleDareGod.getDamage() == 0L) {
            return;
        }
        dareGodCaches.add(tmpDareGodCache);
        Collections.sort(dareGodCaches);
        if (dareGodCaches.size() > DareGodConst.MAX_RANK) {
            List<RankRoleDareGodCache> tmp = dareGodCaches.subList(0, DareGodConst.MAX_RANK);
            roleDareRankList.put(fightType, new LinkedList<>(tmp));
        }
    }

    @Override
    public void getTargetAward(int serverId, int mainServerId, long roleId, int targetId) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            LogUtil.info("挑战女神收取目标奖励异常|找不到玩家|roleId:{},mainServerId:{},MapSize:{}", roleId, mainServerId, roleDareGodMap.size());
            return;
        }
        SsbBoss ssbBoss = DareGodManager.getSsbBossByType(getSubServer(mainServerId), roleDareGod.getFightType());
        if (ssbBoss == null) {
            LogUtil.info("挑战女神收取目标奖励异常|找不到产品数据|roleId:{},mainServerId:{},fightType:{}", roleId, mainServerId, roleDareGod.getFightType());
            return;
        }
        if (!roleDareGod.getTargetDropMap().containsKey(targetId)) {
            LogUtil.info("挑战女神收取目标奖励异常|错误的伤害值|roleId:{},mainServerId:{},targetDamage:{},roleDamageDropMap:{}", roleId, mainServerId, targetId, roleDareGod.getTargetDropMap());
            return;
        }
        if (roleDareGod.getTargetDropMap().get(targetId) != DareGodConst.UN_GETED) {
            LogUtil.info("挑战女神收取目标奖励异常|不符合领取条件|roleId:{},mainServerId:{},targetDamage:{},roleDamageDropMap:{}", roleId, mainServerId, targetId, roleDareGod.getTargetDropMap());
            return;
        }
        roleDareGod.updateDamageDropState(targetId, DareGodConst.GETED);
        rowDao.update(roleDareGod);
        int dropId = ssbBoss.getTargetDropGroupMap().get(targetId);
        Map<Integer, Integer> itemMap = DropUtil.executeDrop(dropId, 1);
        ChatRpcHelper.getRoleService().notice(mainServerId, roleId, new DareGodGetAwardEvent(itemMap));
        ClientAward clientAward = new ClientAward();
        clientAward.setAwrd(itemMap);
        clientAward.setType((byte) 1);
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientAward);
        ClientDareGod clientDareGod = new ClientDareGod(ClientDareGod.UPDATE_DAMAGE_MAP);
        clientDareGod.setTargetDropMap(roleDareGod.getTargetDropMap());
        clientDareGod.setTargetDropGroupMap(ssbBoss.getTargetDropGroupMap());
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientDareGod);
    }

    @Override
    public void buyTimes(int serverId, int mainServerId, long roleId, int vipLv, int buyTime) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            LogUtil.info("挑战女神购买次数异常|找不到玩家|roleId:{},mainServerId:{},MapSize:{}", roleId, mainServerId, roleDareGodMap.size());
            return;
        }
        if (DareGodManager.getCanBuyTimes(vipLv) - roleDareGod.getBuyTimes() < buyTime) {
            LogUtil.info("挑战女神购买次数异常|超出购买次数|roleId:{},mainServerId:{},vipLv:{},time:{}", roleId, mainServerId, vipLv, roleDareGod.getBuyTimes());
            Map<Integer, Integer> tmp = new HashMap<>();
            tmp.put(ToolManager.BANDGOLD, buyTime * DareGodManager.BUY_TIMES_REQ_ITEM_COUNT);
            ChatRpcHelper.getRoleService().notice(mainServerId, roleId, new DareGodGetAwardEvent(tmp));
            ChatRpcHelper.getRoleService().warn(mainServerId, roleId, "ssb_tips_csdsx");
            return;
        }
        roleDareGod.addBuyAndCanFightTimes(buyTime);
        rowDao.update(roleDareGod);
        ClientDareGod clientDareGod = new ClientDareGod(ClientDareGod.UPDATE_TIMES);
        clientDareGod.setCanFightTimes(roleDareGod.getCanFightTimes());
        clientDareGod.setCanBuyTimes(DareGodManager.getCanBuyTimes(vipLv) - roleDareGod.getBuyTimes());
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientDareGod);
        ChatRpcHelper.getRoleService().warn(mainServerId, roleId, "ssb_tips_gmcg");
    }

    @Override
    public void enterFight(int serverId, int mainServerId, long roleId) {
        if (!canFight) {
            ChatRpcHelper.getRoleService().send(mainServerId, roleId, new ClientText("正在准备统计发奖"));
            return;
        }
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            LogUtil.info("挑战女神进入战斗异常|找不到玩家|roleId:{},mainServerId:{},MapSize:{}", roleId, mainServerId, roleDareGodMap.size());
            return;
        }
        if (roleDareGod.getCanFightTimes() <= 0) {
            ChatRpcHelper.getRoleService().send(mainServerId, roleId, new ClientText("没有挑战次数了"));
            return;
        }
        SsbBoss ssbBoss = DareGodManager.getSsbBossByType(getSubServer(mainServerId), roleDareGod.getFightType());
        if (ssbBoss == null) {
            LogUtil.info("挑战女神进入战斗异常|找不到产品数据|roleId:{},mainServerId:{},fightType:{}", roleId, mainServerId, roleDareGod.getFightType());
            return;
        }
        ChatRpcHelper.getRoleService().notice(mainServerId, roleId, new DareGodEnterFightEvent(ssbBoss.getStageId(), roleDareGod.getFightType(), ssbBoss.getStageMonsterId()));
        LinkedList<RankRoleDareGodCache> roleDareGods = roleDareRankList.get(roleDareGod.getFightType());
        Collections.sort(roleDareGods);
        LinkedList<RankRoleDareGodCache> cacheLinkedList = new LinkedList<>();
        for (int i = 0; i < roleDareGods.size() && cacheLinkedList.size() < DareGodConst.MAX_RANK_FOR_FIGHT; i++) {
            RankRoleDareGodCache dareGodCache = roleDareGods.get(i);
            cacheLinkedList.add(dareGodCache);

        }
        RankRoleDareGodCache myDareGodCache = new RankRoleDareGodCache(roleId);
        ClientDareGod clientDareGod = new ClientDareGod(ClientDareGod.AFTER_ENTER_FIGHT);
        clientDareGod.setTotalDamage(roleDareGod.getDamage());
        clientDareGod.setMyRank(roleDareGods.contains(myDareGodCache) ? roleDareGods.indexOf(myDareGodCache) + 1 : -1);
        clientDareGod.setDareGodCaches(cacheLinkedList);
        ChatRpcHelper.getRoleService().send(mainServerId, roleId, clientDareGod);
    }

    @Override
    public void delFightTime(int serverId, int mainServerId, long roleId, int time) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            LogUtil.info("挑战女神扣除次数异常|找不到玩家|roleId:{},mainServerId:{},MapSize:{}", roleId, mainServerId, roleDareGodMap.size());
            return;
        }
        roleDareGod.setCanFightTimes(roleDareGod.getCanFightTimes() - time);
        rowDao.update(roleDareGod);
    }

    @Override
    public void registerServer(int serverId, int mainServerId) {
        int subServer = getSubServer(mainServerId);
        if (!subServerMap.containsKey(subServer)) {
            this.subServerMap.put(getSubServer(mainServerId), false);
            loadUserdata(subServer);
        }
        LogUtil.info("mainServerId:{}--注册--subServer:{}", mainServerId, subServerMap);
    }

    @Override
    public void save() {
        rowDao.flush();
    }

    @Override
    public void updateRoleName(int serverId, long roleId, String roleName) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            return;
        }
        roleDareGod.setRoleName(roleName);
        rowDao.update(roleDareGod);
        if (roleDareGod.getFightType() == 0)
            return;
        LinkedList<RankRoleDareGodCache> cacheLinkedList = roleDareRankList.get(roleDareGod.getFightType());
        RankRoleDareGodCache cache = new RankRoleDareGodCache(roleId);
        if (cacheLinkedList.contains(cache)) {
            RankRoleDareGodCache roleDareGodCache = cacheLinkedList.get(cacheLinkedList.indexOf(cache));
            roleDareGodCache.setRoleName(roleName);
        }
    }

    @Override
    public void updateFightScore(int serverId, long roleId, int fightScore) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            return;
        }
        roleDareGod.setFightScore(fightScore);
        rowDao.update(roleDareGod);
        if (roleDareGod.getFightType() == 0)
            return;
        LinkedList<RankRoleDareGodCache> cacheLinkedList = roleDareRankList.get(roleDareGod.getFightType());
        RankRoleDareGodCache cache = new RankRoleDareGodCache(roleId);
        if (cacheLinkedList.contains(cache)) {
            RankRoleDareGodCache roleDareGodCache = cacheLinkedList.get(cacheLinkedList.indexOf(cache));
            roleDareGodCache.setFightScore(fightScore);
        }
    }

    @Override
    public void updateFashionId(int serverId, long roleId, int fashionId) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            return;
        }
        roleDareGod.setFashionId(fashionId);
        rowDao.update(roleDareGod);
        if (roleDareGod.getFightType() == 0)
            return;
        LinkedList<RankRoleDareGodCache> cacheLinkedList = roleDareRankList.get(roleDareGod.getFightType());
        RankRoleDareGodCache cache = new RankRoleDareGodCache(roleId);
        if (cacheLinkedList.contains(cache)) {
            RankRoleDareGodCache roleDareGodCache = cacheLinkedList.get(cacheLinkedList.indexOf(cache));
            roleDareGodCache.setFashionId(fashionId);
        }
    }

    @Override
    public void updateJobId(int serverId, long roleId, int newJobId) {
        RoleDareGod roleDareGod = roleDareGodMap.get(roleId);
        if (roleDareGod == null) {
            return;
        }
        roleDareGod.setJobId(newJobId);
        rowDao.update(roleDareGod);
        if (roleDareGod.getFightType() == 0)
            return;
        LinkedList<RankRoleDareGodCache> cacheLinkedList = roleDareRankList.get(roleDareGod.getFightType());
        RankRoleDareGodCache cache = new RankRoleDareGodCache(roleId);
        if (cacheLinkedList.contains(cache)) {
            RankRoleDareGodCache roleDareGodCache = cacheLinkedList.get(cacheLinkedList.indexOf(cache));
            roleDareGodCache.setJobId(newJobId);
        }
    }

    private void updateDamageDropMap(SsbBoss ssbBoss, RoleDareGod roleDareGod) {
        for (int targetId : ssbBoss.getDamageTargetSet()) {
            SsbBossTarget bossTarget = DareGodManager.ssbBossTargetMap.get(targetId);
            if (bossTarget == null) continue;
            try {
                if (roleDareGod.getTargetDropMap().get(targetId) == DareGodConst.CANT_GET
                        && roleDareGod.getDamage() >= bossTarget.getTargetHurt()) {
                    roleDareGod.updateDamageDropState(targetId, DareGodConst.UN_GETED);
                }
            } catch (Exception e) {
                LogUtil.info("updateDamageDropMap|targetId:{},targetMap:{}", targetId, roleDareGod.getTargetDropMap());
                e.printStackTrace();
            }
        }
    }

    private void loadUserdata(int subServer) {
        if (subServerMap.get(subServer)) return;
        try {
            subServerMap.put(subServer, true);
            Map<Long, RoleDareGod> dareGodMap = DBUtil.queryMap(DBUtil.DB_COMMON, "roleid", RoleDareGod.class, "select * from roledaregod where floor( `serverid`/1000) =" + subServer);
            this.roleDareGodMap.putAll(dareGodMap);
            Map<Integer, LinkedList<RankRoleDareGodCache>> tmpRankList = new LinkedHashMap<>();
            for (RoleDareGod roleDareGod : dareGodMap.values()) {
                if (roleDareGod.getFightType() != 0 && roleDareGod.getDamage() != 0L) {
                    LinkedList<RankRoleDareGodCache> roleDareGodLinkedList = tmpRankList.get(roleDareGod.getFightType());
                    RankRoleDareGodCache cache = new RankRoleDareGodCache(roleDareGod.getRoleId(), roleDareGod.getServerId(), roleDareGod.getRoleName(),
                            roleDareGod.getDamage(), roleDareGod.getFightScore(), roleDareGod.getFightType(), roleDareGod.getFashionId(), roleDareGod.getJobId());
                    if (roleDareGodLinkedList == null) {
                        roleDareGodLinkedList = new LinkedList<>();
                        tmpRankList.put(roleDareGod.getFightType(), roleDareGodLinkedList);
                    }
                    if (!roleDareGodLinkedList.contains(cache)) {
                        roleDareGodLinkedList.add(cache);
                    }
                    Collections.sort(roleDareGodLinkedList);
                    if (roleDareGodLinkedList.size() > DareGodConst.MAX_RANK) {
                        List<RankRoleDareGodCache> tmp = roleDareGodLinkedList.subList(0, DareGodConst.MAX_RANK);
                        tmpRankList.put(roleDareGod.getFightType(), new LinkedList<>(tmp));
                    }
                }
            }
            for (Map.Entry<Integer, LinkedList<RankRoleDareGodCache>> entry : tmpRankList.entrySet()) {
                roleDareRankList.get(entry.getKey()).addAll(entry.getValue());
            }
            LogUtil.info("roleDareRankList:{}", roleDareRankList.keySet());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillRankListByFightType() {
        for (int fightType : DareGodManager.fightTypeSet) {
            roleDareRankList.put(fightType, new LinkedList<RankRoleDareGodCache>());
        }
    }
}
