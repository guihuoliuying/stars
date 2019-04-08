package com.stars.modules.bestcp520;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.bestcp520.packet.ClientBestCPPacket;
import com.stars.modules.bestcp520.userdata.RoleBestCPReward;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.util.DateUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPModule extends AbstractModule implements OpActivityModule {
    private Map<String, RoleBestCPReward> roleBestCPRewardMap;
    private RoleBestCPReward roleBestCPReward;
    private Map<Integer, Integer> myCPRank = new HashMap<>();
    DropModule dropModule = module(MConst.Drop);
    ToolModule toolModule = module(MConst.Tool);

    public BestCPModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("最佳组合", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        if (getCurShowActivityId() == -1) {
            return;
        }
        /**
         * 领奖记录
         */
        String rewardsql = "select * from rolebestcpreward where roleid=" + id();
        if (roleBestCPRewardMap == null) {
            roleBestCPRewardMap = DBUtil.queryConcurrentMap(DBUtil.DB_USER, "date", RoleBestCPReward.class, rewardsql);
        }

    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleBestCPRewardMap = new HashMap<>();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
        if (operateActVo != null) {
            /**
             * 获取活动界面规则及其时间描述
             */
            String ruledescKey = operateActVo.getRuledesc();
            String timedesckey = operateActVo.getTimedesc();
            ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
            BestCPManager.ruleDesc = DataManager.getGametext(ruledescKey);
            BestCPManager.timeDesc = String.format(DataManager.getGametext(timedesckey), actOpenTime5.getStartDateString(), actOpenTime5.getEndDateString());

            String date = DateUtil.formatDate(new Date(), DateUtil.YMD_);
            roleBestCPReward = roleBestCPRewardMap.get(date);
            if (roleBestCPReward == null) {
                roleBestCPReward = new RoleBestCPReward(id(), date, "", 0);
                context().insert(roleBestCPReward);
                roleBestCPRewardMap.put(date, roleBestCPReward);
            }
            signCalRedPoint(MConst.BestCP520, RedPointConst.BEST_CP520);
        }

    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.BEST_CP520)) {
            if (canTakeRewardNum() > 0) {
                redPointMap.put(RedPointConst.BEST_CP520, "");
            } else {
                redPointMap.put(RedPointConst.BEST_CP520, null);
            }
        }
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_BestCP520);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            /**
             * 角色是否被限制
             */
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            }

        }
        return -1;
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (getCurShowActivityId() == -1) {
            return;
        }
        /**
         * 每日重置，重新创建领奖记录
         */
        String date = DateUtil.formatDate(new Date(), DateUtil.YMD_);
        roleBestCPReward = roleBestCPRewardMap.get(date);
        if (roleBestCPReward == null) {
            roleBestCPReward = new RoleBestCPReward(id(), date, "", 0);
            context().insert(roleBestCPReward);
            roleBestCPRewardMap.put(date, roleBestCPReward);
        }
        sendCanTakeReward();
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    /**
     * 领奖
     *
     * @param section 投票数量
     */
    public void takeReward(int section) {
        String date = DateUtil.formatDate(new Date(), DateUtil.YMD_);
        roleBestCPReward = roleBestCPRewardMap.get(date);
        if (roleBestCPReward.getVoteSum() < section) {
            return;
        }
        if (roleBestCPReward.getRewardMap().get(section) == null) {
            Integer groupId = BestCPManager.groupRewardMap.get(section);
            Map<Integer, Integer> reward = dropModule.executeDrop(groupId, 1, true);
            toolModule.addAndSend(reward, EventType.AWARD.getCode());
            ClientAward clientAward = new ClientAward(reward);
            clientAward.setType((byte) 1);
            toolModule.sendPacket(clientAward);
            roleBestCPReward.putReward(section, 0);
            context().update(roleBestCPReward);
            sendCanTakeReward();
            signCalRedPoint(MConst.BestCP520, RedPointConst.BEST_CP520);
        } else {
            warn("奖励已领取");
        }

    }

    private int canTakeRewardNum() {
        int canTakeRewardNum = 0;
        for (Integer section : BestCPManager.groupRewardMap.keySet()) {
            int voteSum = roleBestCPReward.getVoteSum();
            if (voteSum >= section) {
                if (!roleBestCPReward.getRewardMap().containsKey(section)) {
                    canTakeRewardNum++;
                }
            }
        }
        return canTakeRewardNum;
    }

    /**
     * 下发能领取的宝箱数量
     */
    public void sendCanTakeReward() {
        ClientBestCPPacket clientBestCPPacket = new ClientBestCPPacket(ClientBestCPPacket.CAN_TAKE_REWARD);
        Set<Integer> taked = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : roleBestCPReward.getRewardMap().entrySet()) {
            if (entry.getValue() == 0) {
                taked.add(entry.getKey());
            }
        }
        clientBestCPPacket.setTakedGroup(taked);
        clientBestCPPacket.setVoteSum(roleBestCPReward.getVoteSum());
        send(clientBestCPPacket);
    }

    /**
     * 给指定组合投票
     *
     * @param cpId
     */
    public void vote(Integer cpId) {

        boolean success = toolModule.deleteAndSend(BestCPManager.bestCPTicketItemId, 1, EventType.USETOOL.getCode());
        if (success) {
            ServiceHelper.opBestCPService().vote(id(), cpId);
            String date = DateUtil.formatDate(new Date(), DateUtil.YMD_);
            roleBestCPReward = roleBestCPRewardMap.get(date);
            roleBestCPReward.setVoteSum(roleBestCPReward.getVoteSum() + 1);
            context().update(roleBestCPReward);
            warn(DataManager.getGametext("bestcp_ticks_votesuc"));

            sendCanTakeReward();
            sendBestCPRank();
            signCalRedPoint(MConst.BestCP520, RedPointConst.BEST_CP520);
        } else {
            warn("缺少相关道具，投票失败");
        }
    }

    /**
     * 发送最佳cp排行榜
     */
    public void sendBestCPRank() {
        List<AbstractRankPo> frontRank = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_BEST_CP, 10);
        ClientBestCPPacket clientBestCPPacket = new ClientBestCPPacket(ClientBestCPPacket.BEST_CP_RANK);
        clientBestCPPacket.setRoleId(id());
        clientBestCPPacket.setBestCPRankList(frontRank);
        clientBestCPPacket.setMyCPRank(myCPRank);
        send(clientBestCPPacket);

    }

    /**
     * 发送指定cp下角色投票排行榜
     *
     * @param cpId
     */
    public void sendBestCPVoterRank(int cpId) {
        ServiceHelper.rankService().sendRankList4BestCPVoter(RankConstant.RANKID_BEST_CP_VOTER, id(), cpId);
    }

    /**
     * 下发活动界面配置数据
     */
    public void sendActivityUI() {
        ClientBestCPPacket clientBestCPPacket = new ClientBestCPPacket(ClientBestCPPacket.SEND_ACTIVITY_UI);
        send(clientBestCPPacket);

    }


}
