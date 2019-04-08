package com.stars.modules.offlinepvp;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.offlinepvp.packet.ClientOfflinePvpData;
import com.stars.modules.offlinepvp.prodata.OPMatchVo;
import com.stars.modules.offlinepvp.prodata.OPRewardVo;
import com.stars.modules.offlinepvp.userdata.RoleOfflinePvp;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.util.DateUtil;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/9/30.
 */
public class OfflinePvpModule extends AbstractModule {
    private RoleOfflinePvp roleOfflinePvp;
    private Map<Byte, OPEnemyCache> enemyMap = new HashMap<>();// 挑战对手,<index, OPEnemyCache>
    private Set<Byte> canGetPreciousList = new HashSet<>();
    public OfflinePvpModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("离线pvp", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleOfflinePvp = new RoleOfflinePvp(id());
        roleOfflinePvp.setChallegedNum(OfflinePvpManager.initChallengeLimit);
        context().insert(roleOfflinePvp);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleOfflinePvp = DBUtil.queryBean(DBUtil.DB_USER, RoleOfflinePvp.class,
                "select * from `roleofflinepvp` where `roleid`=" + id());
        if (roleOfflinePvp == null) {
            roleOfflinePvp = new RoleOfflinePvp(id());
            roleOfflinePvp.setChallegedNum(OfflinePvpManager.initChallengeLimit);
            context().insert(roleOfflinePvp);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        roleOfflinePvp.setRefreshedNum((byte) 0);
        int newChallegeNum = roleOfflinePvp.getChallegedNum() > OfflinePvpManager.initChallengeLimit ? 
        		roleOfflinePvp.getChallegedNum() : OfflinePvpManager.initChallengeLimit;
        roleOfflinePvp.setChallegedNum(newChallegeNum);
        roleOfflinePvp.setBuyRefreshNum((byte) 0);
        roleOfflinePvp.setBuyChallengeNum((byte) 0);
        roleOfflinePvp.setAutoRefreshNum((byte) 0);
        context().update(roleOfflinePvp);
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.RESET_DAILY);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        send(packet);

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
//        autoRefreshEnemy();
//        canGetPrecious();
    }

    @Override
    public void onTimingExecute() {
//        if (autoRefreshEnemy()) {
//            sendNewEnemy();
//        }
    }

    /**
     * 推送更新等级和战力,作为明天基准战力使用
     */
    public void updateStandard(int level, int fightScore) {
        ServiceHelper.offlinePvpService().updateUseStandard(id(), level, fightScore);
    }

    /**
     * 打开界面,请求所有数据
     */
    public void reqAllData() {
        if (roleOfflinePvp.getMatchVoId() == 0 || roleOfflinePvp.getStandardLevel() == 0 ||
                roleOfflinePvp.getStandardFightScore() == 0) {
            updateMatchStandard();
        }
        if (enemyMap.isEmpty()) {
            enemyMap = ServiceHelper.offlinePvpService().getMatchEnemys(id(), roleOfflinePvp.getMatchVoId(),
                    roleOfflinePvp.getStandardLevel());
        }
        autoRefreshEnemy();
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.ALL);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        packet.setEnemyMap(enemyMap);
        send(packet);
    }

    /**
     * 手动领奖
     *
     * @param index
     */
    public void reward(byte index) {
        // 已领取
        if (roleOfflinePvp.getRewardedIndexSet().contains(index))
            return;
        // 已战胜对手个数不足
        if (roleOfflinePvp.getWinEnemyNum() < index)
            return;
        Map<Integer, Integer> rewardMap = OfflinePvpManager.getOPRewardVo(roleOfflinePvp.getStandardLevel()).getRewardMap(index);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(rewardMap,EventType.OFFLINEPVP.getCode());
        roleOfflinePvp.addRewardIndex(index);
        context().update(roleOfflinePvp);
        // update to client
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.REWARD);
        packet.setRewardedIndex(index);
        send(packet);
        canGetPrecious();
    }

    public void canGetPrecious(){
        OPRewardVo opRewardVo = OfflinePvpManager.getOPRewardVo(roleOfflinePvp.getStandardLevel());
        if(opRewardVo==null) return;
        for(byte index : opRewardVo.getRewardMap().keySet()){
            if(roleOfflinePvp.getWinEnemyNum()>=index && !roleOfflinePvp.getRewardedIndexSet().contains(index)){
                canGetPreciousList.add(index);
            }
            else{
                if(canGetPreciousList.contains(index)){
                    canGetPreciousList.remove(index);
                }
            }
        }
        signCalRedPoint(MConst.OfflinePvp,RedPointConst.OFFLINE_PVP_GET_PRECIOUS);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if(redPointIds.contains(Integer.valueOf(RedPointConst.OFFLINE_PVP_GET_PRECIOUS))){
            checkRedPoint(redPointMap,canGetPreciousList,RedPointConst.OFFLINE_PVP_GET_PRECIOUS);
        }
    }
    private void checkRedPoint(Map<Integer, String> redPointMap,Set<Byte> list,int redPointConst){
        StringBuilder builder = new StringBuilder("");
        if(!list.isEmpty()){
            Iterator<Byte> iterator = list.iterator();
            while (iterator.hasNext()){
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst,builder.toString().isEmpty() ? null : "");
        }
        else {
            redPointMap.put(redPointConst, null );
        }
    }
    /**
     * 手动刷新
     */
    public void executeRefreshEnemy() {
        if (roleOfflinePvp.getRefreshedNum() >= OfflinePvpManager.initRefreshLimit)
            return;
        roleOfflinePvp.setRefreshedNum((byte) (roleOfflinePvp.getRefreshedNum() + 1));
        refreshEnemy();
        sendNewEnemy();
    }

    /**
     * 购买刷新次数
     */
    public void buyRefreshNum() {
        if (roleOfflinePvp.getBuyRefreshNum() >= OfflinePvpManager.buyRefreshLimit)
            return;
        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(OfflinePvpManager.getBuyRefreshCost(roleOfflinePvp.getBuyRefreshNum() + 1),EventType.REFRESHNUM.getCode()))
            return;
        roleOfflinePvp.setRefreshedNum((byte) (roleOfflinePvp.getRefreshedNum() - 1));
        roleOfflinePvp.setBuyRefreshNum((byte) (roleOfflinePvp.getBuyRefreshNum() + 1));
        context().update(roleOfflinePvp);
        // update to client
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.BUY_REFRESH);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        send(packet);
    }

    /**
     * 购买挑战次数
     */
    public void buyChallengeNum() {
        if (roleOfflinePvp.getBuyChallengeNum() >= OfflinePvpManager.buyChallengeLimit)
            return;
//        ToolModule toolModule = module(MConst.Tool);
//        if (!toolModule.deleteAndSend(OfflinePvpManager.getBuyChallengeCost(roleOfflinePvp.getBuyChallengeNum() + 1)))
//            return;
//        roleOfflinePvp.setChallegedNum((byte) (roleOfflinePvp.getChallegedNum() - 1));
//        roleOfflinePvp.setBuyChallengeNum((byte) (roleOfflinePvp.getBuyChallengeNum() + 1));
        
        int buyIndex = roleOfflinePvp.getBuyChallengeNum() + 1;
        Map<Integer, Integer> cost = OfflinePvpManager.getBuyChallengeCost(buyIndex);    
        ToolModule toolModule = module(MConst.Tool);
        if (cost == null || !toolModule.deleteAndSend(cost,EventType.BUYNUM.getCode()))
            return;
        roleOfflinePvp.setChallegedNum(roleOfflinePvp.getChallegedNum() + OfflinePvpManager.perBuyChallengeNum);
        roleOfflinePvp.setBuyChallengeNum((byte) (roleOfflinePvp.getBuyChallengeNum() + 1));
        
        context().update(roleOfflinePvp);
        // update to client
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.BUY_CHALLENGE);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        send(packet);
    }

    /**
     * 获得成长buff
     */
    public Map<Integer, Integer> getGrowBuff() {
        OPMatchVo matchVo = OfflinePvpManager.getOpMatchVo(roleOfflinePvp.getMatchVoId());
        RoleModule roleModule = module(MConst.Role);
        int curFScore = roleModule.getRoleRow().getFightScore();
        int standardFScore = roleOfflinePvp.getStandardFightScore();
        // todo:这里第二个条件要判断充值达到多少金额
        if ((curFScore - standardFScore) / standardFScore * 100.0 >= matchVo.getBuffCondPercent() || false) {
            return matchVo.getGrowBuffMap();
        }
        return new HashMap<>();
    }

    /**
     * 对手是否可以挑战
     *
     * @param enemyIndex
     * @return
     */
    public boolean canChallenge(byte enemyIndex) {
        // 挑战次数
//        if (roleOfflinePvp.getChallegedNum() >= OfflinePvpManager.initChallengeLimit)
//            return false;
    	if (roleOfflinePvp.getChallegedNum() <= 0)
            return false;
        // 是否战胜过
        if (roleOfflinePvp.getWinIndexSet().contains(enemyIndex))
            return false;
        return true;
    }

    /**
     * 获得使用战斗场景Id
     *
     * @return
     */
    public int getFightStageId() {
        RoleModule roleModule = module(MConst.Role);
        for (Map.Entry<Integer, Integer> entry : OfflinePvpManager.useStage.entrySet()) {
            if (roleModule.getLevel() <= entry.getKey()) {
                return entry.getValue();
            }
        }
        return 0;
    }

    /**
     * 获得对手战斗实体(角色+伙伴)
     *
     * @param index
     * @return
     */
    public Map<String, FighterEntity> getEnemyFighterEntity(byte index) {
        return enemyMap.get(index).getEntityMap();
    }

    /**
     * 获得单次胜利奖励
     *
     * @return
     */
    public Map<Integer, Integer> getVictoryReward() {
        OPRewardVo rewardVo = OfflinePvpManager.getOPRewardVo(roleOfflinePvp.getStandardLevel());
        return rewardVo.getOnceRewardMap();
    }

    /**
     * 抛出事件
     *
     * @param event
     */
    public void dispatchEvent(Event event) {
        eventDispatcher().fire(event);
    }

    /**
     * 战斗胜利
     *
     * @param enemyIndex
     */
    public void victory(byte enemyIndex) {
        roleOfflinePvp.addWinIndex(enemyIndex);
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.CHALLENGE_WIN);
        packet.setWinIndex(enemyIndex);
        send(packet);
    }

    /**
     * 增加挑战次数
     */
//    public void addChallegeCount() {
//        roleOfflinePvp.setChallegedNum((byte) (roleOfflinePvp.getChallegedNum() + 1));
//        context().update(roleOfflinePvp);
//        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.CHALLENGE_NUMBER);
//        packet.setRoleOfflinePvp(roleOfflinePvp);
//        send(packet);
//    }
    
    public void addChallegeCount() {//notice:本来是添加挑战次数，这里改为减少剩余次数
        roleOfflinePvp.setChallegedNum(roleOfflinePvp.getChallegedNum() - 1);
        context().update(roleOfflinePvp);
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.CHALLENGE_NUMBER);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        send(packet);
    }

    /* 更新匹配matchVoId,基准等级(奖励等级id),基准战力 */
    private void updateMatchStandard() {
        RoleModule roleModule = module(MConst.Role);
        int[] standard = ServiceHelper.offlinePvpService().getMatchAndRewardId(id(), roleModule.getRoleRow().getJobId());
        boolean isChanged = false;
        if (roleOfflinePvp.getMatchVoId() != standard[0]) {
            roleOfflinePvp.setMatchVoId(standard[0]);
            isChanged = true;
        }
        if (roleOfflinePvp.getStandardLevel() != standard[1]) {
            roleOfflinePvp.setStandardLevel(standard[1]);
            isChanged = true;
        }
        if (roleOfflinePvp.getRefreshedNum() == 0 && roleOfflinePvp.getAutoRefreshNum() == 0 &&
                roleOfflinePvp.getStandardFightScore() != standard[2]) {
            roleOfflinePvp.setStandardFightScore(standard[2]);
            isChanged = true;
        }
        if (isChanged)
            context().update(roleOfflinePvp);
    }

    /* 执行刷新对手 */
    private void refreshEnemy() {
        // 检查有没有未领取的奖励
        Map<Integer, Integer> rewardMap = new HashMap<>();
        OPRewardVo rewardVo = OfflinePvpManager.getOPRewardVo(roleOfflinePvp.getStandardLevel());
        if (rewardVo != null) {
            for (byte index : rewardVo.getRewardMap().keySet()) {
                if (roleOfflinePvp.getRewardedIndexSet().contains(index))
                    continue;
                if (roleOfflinePvp.getWinEnemyNum() < index)
                    break;
                MapUtil.add(rewardMap, rewardVo.getRewardMap(index));
            }
            if (!rewardMap.isEmpty()) {
                ServiceHelper.emailService().sendToSingle(id(), 13001, Long.valueOf(roleOfflinePvp.getStandardLevel()),
                        "演武场管理员", rewardMap);
            }
        }
        roleOfflinePvp.clearWinIndex();
        roleOfflinePvp.clearRewardIndex();
        // 更新基准战力
        RoleModule roleModule = module(MConst.Role);
        roleOfflinePvp.setStandardFightScore(roleModule.getRoleRow().getFightScore());
        context().update(roleOfflinePvp);
        updateMatchStandard();
        enemyMap = ServiceHelper.offlinePvpService().refreshMatchEnemys(id(), roleOfflinePvp.getMatchVoId(),
                roleOfflinePvp.getStandardLevel());
        context().update(roleOfflinePvp);
        canGetPrecious();
    }

    /**
     * 下发刷新后的对手
     */
    private void sendNewEnemy() {
        // update to client
        ClientOfflinePvpData packet = new ClientOfflinePvpData(ClientOfflinePvpData.ALL);
        packet.setRoleOfflinePvp(roleOfflinePvp);
        packet.setEnemyMap(enemyMap);
        send(packet);
    }

    /* 自动刷新对手 */
    private boolean autoRefreshEnemy() {
        if (roleOfflinePvp == null || roleOfflinePvp.getAutoRefreshNum() >= 2) {
            return false;
        }
        long baseTime = DateUtil.hourStrTimeToDateTime(DataManager.DAILY_RESET_TIME_STR).getTime();
        long refreshTimestamp = baseTime + OfflinePvpManager.autoRefreshInterval[roleOfflinePvp.getAutoRefreshNum()];
        if (System.currentTimeMillis() < refreshTimestamp) {
            return false;
        }
        roleOfflinePvp.setAutoRefreshNum((byte) (roleOfflinePvp.getAutoRefreshNum() + 1));
        refreshEnemy();
        // 如果时间超过第二次刷新,则直接将已自动刷新次数置为2
        if (System.currentTimeMillis() >= baseTime + OfflinePvpManager.autoRefreshInterval[1]) {
            roleOfflinePvp.setAutoRefreshNum((byte) 2);
        }
        context().update(roleOfflinePvp);
        return true;
    }

    public RoleOfflinePvp getRoleOfflinePvp() {
        return roleOfflinePvp;
    }
}
