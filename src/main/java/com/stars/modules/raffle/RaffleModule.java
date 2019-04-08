package com.stars.modules.raffle;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.raffle.define.RaffleCommonConfig;
import com.stars.modules.raffle.define.RaffleDefineManager;
import com.stars.modules.raffle.define.RaffleRewardEntry;
import com.stars.modules.raffle.helper.RaffleHelper;
import com.stars.modules.raffle.packet.ClientRaffleDo;
import com.stars.modules.raffle.packet.ClientRaffleGetInfo;
import com.stars.modules.raffle.packet.ClientRaffleSelectReward;
import com.stars.modules.raffle.userdata.RoleRaffleInfo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipModule;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang by 2017/4/22
 */

public class RaffleModule extends AbstractModule {

    /**
     * 1.轮结束，重新初始化数据
     */

    private RoleRaffleInfo info;

    public RaffleModule(String name, long id, Player self, EventDispatcher eventDispatcher,
                        Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String selectSql = String.format("select * from roleraffleinfo where roleid = %s;", id());
        info = DBUtil.queryBean(DBUtil.DB_USER, RoleRaffleInfo.class, selectSql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        super.onInit(isCreation);
        if (info == null) {
            info = new RoleRaffleInfo(id());
            int vipLevel = getVipLevel();
            info.init(vipLevel);
            context().insert(info);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        info = new RoleRaffleInfo(id());
        int vipLevel = getVipLevel();
        info.init(vipLevel);
        context().insert(info);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        info.dailyReset();
        context().update(info);
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        info.weekReset();
        context().update(info);
    }

    // 拿取玩家状态数据
    public void getInfo() {
        ClientRaffleGetInfo res = new ClientRaffleGetInfo();
        res.setUserTimes(info.getUserTimes());
        res.setPosition(info.getPosition());
        res.setTotalMoney(info.getTotalMoney());
        res.setDailyLeftTimes(info.getDailyLeftTimes());
        int rewardIndex = info.getRewardIndex();
        List<RaffleRewardEntry> entryList = RaffleDefineManager.instance.getRewardEntrysBy(rewardIndex);
        res.setRewardEntry(entryList);
        res.setSpeed(info.getSpeedList());
        send(res);

        if(info.getLastSuperPosition() != 0){ //十连抽过程有可选择奖励
            ClientRaffleSelectReward res1 = new ClientRaffleSelectReward();
            List<RaffleRewardEntry> entryList1 = RaffleDefineManager.instance.getRewardEntrysBy(info.getLastSuperRewardIndex());
            res1.setRewardEntry(entryList1);
            res1.setSubType(ClientRaffleSelectReward.SELECT_REWARD_TEN_TIME);
            res1.setMoney(0);
            send(res1);
        }
        if(info.getLastSuperPosition() == 0 && info.isEnd()) { //单抽最后一步，可领取最终大奖
            // 下发抽奖结果
            ClientRaffleDo res2 = new ClientRaffleDo();
            res2.setPosition(info.getPosition());
            res2.setTotalMoney(info.getTotalMoney());
            send(res2);
        }
    }

    // 抽奖一次
    public void doRaffle() {
        if (!info.validTimes()) {
            send(new ClientText(RaffleCodeConst.HAS_NOT_TIMES, ""));
            return;
        }
        // 验证并扣除物品
        ToolModule toolModule = module(MConst.Tool);
        RaffleCommonConfig.RaffleCost cost = RaffleDefineManager.instance.getCommonConfig().cost;
        if (!toolModule.deleteAndSend(cost.itemId, cost.num, EventType.RAFFLE_COST.getCode())) {
            send(new ClientText(RaffleCodeConst.TOOL_NOT_ENOUGHT, ""));
            return;
        }
        // 修改内存数据并更新到数据库
        info.onRaffle();
        context().update(info);
        sendReward();// 发奖励
        // 下发抽奖结果
        ClientRaffleDo res = new ClientRaffleDo();
        res.setPosition(info.getPosition());
        res.setTotalMoney(info.getTotalMoney());
        send(res);
    }

    // 抽奖十次
    public void doTenRaffle() {
        if (!info.validateTenTimes()) {
            send(new ClientText(RaffleCodeConst.HAS_NOT_TIMES, ""));
            return;
        }
        // 验证并扣除物品
        ToolModule toolModule = module(MConst.Tool);
        RaffleCommonConfig.RaffleCost cost = RaffleDefineManager.instance.getCommonConfig().cost;
        int tenTimesCostNum = RaffleDefineManager.instance.getCommonConfig().raffleCostTenTimes; //十次抽奖扣除
        if (!toolModule.deleteAndSend(cost.itemId, tenTimesCostNum, EventType.RAFFLE_COST.getCode())) {
            send(new ClientText(RaffleCodeConst.TOOL_NOT_ENOUGHT, ""));
            return;
        }
        Map<Integer,Integer> awardMap = new HashMap<>();
        for(int i = 1; i <= 10; i++) {  //循环十个轮次
            // 修改内存数据并更新到数据库
            info.onRaffle();
            context().update(info);
            if (info.isEnd()) { //本轮结束,如果是第十轮结束，可走之前逻辑
                info.setLastSuperPosition(info.getPosition());
                info.setLastSuperRewardIndex(info.getRewardIndex());

                // 轮重置
                int vipLevel = getVipLevel();
                info.reset(vipLevel, 0);
                context().update(info);
                // 下发消息

                if(info.getLastSuperPosition() != 0){ //十连抽过程有可选择奖励
                    ClientRaffleSelectReward res = new ClientRaffleSelectReward();
                    List<RaffleRewardEntry> entryList = RaffleDefineManager.instance.getRewardEntrysBy(info.getLastSuperRewardIndex());
                    res.setRewardEntry(entryList);
                    res.setSubType(ClientRaffleSelectReward.SELECT_REWARD_TEN_TIME);
                    res.setMoney(0); //领取的时候拿
                    send(res);
                }
                continue;
            }
            DropModule dropModule = module(MConst.Drop);
            int rewardIndex = info.getRewardIndex();
            int index = info.getPosition() - 1;
            RaffleRewardEntry rewardEntry = RaffleDefineManager.instance.getRewardEntry(rewardIndex, index);
            if (rewardEntry == null) {

                continue;
            }
            int dropId = rewardEntry.getItemRewardList().get(0);
            Map<Integer, Integer> reward = dropModule.executeDrop(dropId, 1, true);
            MapUtil.add(awardMap,reward);
        }
        toolModule.addAndSend(awardMap, EventType.RAFFLE_REWARD_TEN_TIME.getCode());
        ClientAward clientAward = new ClientAward(awardMap);
        clientAward.setType((byte)1);
        send(clientAward);
        // 下发抽奖结果
//        ClientRaffleDo res = new ClientRaffleDo();
//        res.setPosition(info.getPosition());
//        res.setTotalMoney(info.getTotalMoney());
//        send(res);
    }

    public void checkAndSendSuperReward(int index) {
        if (!info.isEnd()) {
            send(new ClientText(RaffleCodeConst.HAS_NOT_TIMES, ""));
            return;
        }
        if(info.getLastSuperPosition() != 0) //十次抽奖不在这个请求处理
            return;
        int position = info.getPosition();
        int rewardIndex = info.getRewardIndex();
        RaffleRewardEntry rewardEntry = RaffleDefineManager.instance.getRewardEntry(rewardIndex, position - 1);
        List<Integer> drops = rewardEntry.getItemRewardList();
        if (index < 0 || index >= drops.size()) {
            send(new ClientText(RaffleCodeConst.SELECT_DROP_ERROR, ""));
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        int dropId = rewardEntry.getItemRewardList().get(index);
        Map<Integer, Integer> reward = dropModule.executeDrop(dropId, 1, true);
        // 加入元宝奖励
        int totalMoney = info.getTotalMoney();
        float rate = RaffleHelper.findNearRate(totalMoney);
        int gainMoney = (int) (totalMoney * rate);
        if (gainMoney != 0) {
            reward.put(ToolManager.BANDGOLD, gainMoney);
        }
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(reward, EventType.RAFFLE_REWARD.getCode());
        /**
         * 下发奖励提示
         */
        ClientAward clientAward = new ClientAward(reward);
        send(clientAward);
        // 轮重置
        int vipLevel = getVipLevel();
        info.reset(vipLevel, gainMoney);
        context().update(info);
        // 下发消息
        ClientRaffleSelectReward res = new ClientRaffleSelectReward();
        res.setSubType(ClientRaffleSelectReward.SELECT_REWARD_ONE_TIME);
        res.setMoney(gainMoney);
        send(res);
        /**
         * 数据中心采集日志
         */
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.log_Raffle(position, reward);
    }

    /**
     * 十连抽领取最终奖励
     * @param index
     */
    public void checkAndSendSuperRewardForTenTime(int index) {
        int position = info.getLastSuperPosition();
        int rewardIndex = info.getLastSuperRewardIndex();
        RaffleRewardEntry rewardEntry = RaffleDefineManager.instance.getRewardEntry(rewardIndex, position - 1);
        List<Integer> drops = rewardEntry.getItemRewardList();
        if (index < 0 || index >= drops.size()) {
            send(new ClientText(RaffleCodeConst.SELECT_DROP_ERROR, ""));
            return;
        }
        if(info.getLastSuperPosition() == 0){
            com.stars.util.LogUtil.info("RaffleModule.checkAndSendSuperRewardForTenTime|玩家没有未领取的十连抽最终奖励|roleid:{}",id());
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        int dropId = rewardEntry.getItemRewardList().get(index);
        Map<Integer, Integer> reward = dropModule.executeDrop(dropId, 1, true);
        // 加入元宝奖励
        int totalMoney = info.getTotalMoney();
        float rate = RaffleHelper.findNearRate(totalMoney);
        int gainMoney = (int) (totalMoney * rate);
        if (gainMoney != 0) {
            info.setTotalMoney(totalMoney-gainMoney);
            reward.put(ToolManager.BANDGOLD, gainMoney);
        }
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(reward, EventType.RAFFLE_REWARD.getCode());
        //领取过则改变标识
        info.setLastSuperPosition(0);
        info.setLastSuperRewardIndex(0);
        context().update(info);
        /**
         * 下发奖励提示
         */
        ClientAward clientAward = new ClientAward(reward);
        send(clientAward);
        getInfo(); //更新下发玩家信息
    }

    private void sendReward() {
        // 最后一步是客户端选择奖励
        if (info.isEnd()) {
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        int rewardIndex = info.getRewardIndex();
        int index = info.getPosition() - 1;
        RaffleRewardEntry rewardEntry = RaffleDefineManager.instance.getRewardEntry(rewardIndex, index);
        if (rewardEntry == null) {
            return;
        }
        int dropId = rewardEntry.getItemRewardList().get(0);
        Map<Integer, Integer> reward = dropModule.executeDrop(dropId, 1, true);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(reward, EventType.RAFFLE_REWARD.getCode());
        /**
         * 数据中心采集日志
         */
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.log_Raffle(info.getPosition(), reward);
    }

    private int getVipLevel() {
        VipModule vipModule = module(MConst.Vip);
        if (vipModule == null) {
            LogUtil.error("RaffleModule getVipLevel excpetion vipModule is not init!");
            return 1;// 默认返回1
        }
        return vipModule.getVipLevel();
    }

}
