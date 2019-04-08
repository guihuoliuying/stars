package com.stars.services.newserverfightscore;

import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.newserverfightscore.NewServerFightManager;
import com.stars.modules.newserverfightscore.event.NSFSHistoryRankUpdateEvent;
import com.stars.modules.newserverfightscore.packet.ClientNSFightScore;
import com.stars.modules.newserverfightscore.prodata.NewServerFightScoreVo;
import com.stars.modules.newserverfightscore.userdata.ActRoleNsFightScore;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime3;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.rank.RankManager;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankDisplayVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public class NewServerFightScoreServiceActor extends ServiceActor implements NewServerFightScoreService {
    private int curOperateActId = -1;// 当前活动Id,未开始=-1
    private List<AbstractRankPo> fightScoreRank;// 重置时刻战力排行榜
    private Map<Long, ActRoleNsFightScore> roleRewardRecord;// 角色领奖记录

    @Override
    public void init() throws Throwable {
        fightScoreRank = new LinkedList<>();
        roleRewardRecord = new HashMap<>();
        ServiceSystem.getOrAdd(SConst.NewServerFightScoreService, this);
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerFightScore);
        if (curActId == -1)
            return;
        synchronized (NewServerFightScoreServiceActor.class) {
            String sql = "select * from `actnewserverfightscore` where `operateactid`=" + curActId;
            roleRewardRecord = DBUtil.queryMap(DBUtil.DB_USER, "roleid", ActRoleNsFightScore.class, sql);
        }
        openActivity(curActId);
        updateHistoryRank();
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},fightScoreRank.size:{}" , this.getClass().getSimpleName(),fightScoreRank == null ? 0 : fightScoreRank.size());
        LogUtil.info("容器大小输出:{},roleRewardRecord.size:{}" , this.getClass().getSimpleName(), roleRewardRecord == null ? 0 : roleRewardRecord.size());
    }

    @Override
    public void openActivity(int activityId) {
        this.curOperateActId = activityId;
        fightScoreRank = new LinkedList<>();
    }

    @Override
    public void closeActivity(int activityId) {
        if (curOperateActId != activityId) {
            return;
        }
        this.curOperateActId = -1;
        updateHistoryRank();
        // 遍历领奖记录,邮件补发达标&未领取的奖励
        compensateReward(activityId);
        if (!fightScoreRank.isEmpty()) {
            fightScoreRank.clear();
        }
        if (!roleRewardRecord.isEmpty()) {
            roleRewardRecord.clear();
        }
    }

    @Override
    public void dailyReset() {
        // 更新历史排行榜
        if (curOperateActId == -1)
            return;
        updateHistoryRank();
        ServiceHelper.roleService().noticeAll(new NSFSHistoryRankUpdateEvent());
    }

    private void updateHistoryRank() {
        fightScoreRank = new LinkedList<>();
        RankDisplayVo rankDisplayVo = RankManager.getRankDisplayVo(RankConstant.RANKID_FIGHTSCORE);
        List<AbstractRankPo> historyRank = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FIGHTSCORE,
                rankDisplayVo.getDisCount());
        for (AbstractRankPo rankPo : historyRank) {
            RoleRankPo roleRankPo = (RoleRankPo) rankPo.copy();
            // 没有name,level数据从常用数据拿
            if (StringUtil.isEmpty(roleRankPo.getRoleName()) || roleRankPo.getRoleJobId() == 0) {
                RoleSummaryComponent roleSummary = (RoleSummaryComponent)
                        ServiceHelper.summaryService().getSummaryComponent(roleRankPo.getRoleId(), SummaryConst.C_ROLE);
                roleRankPo.setRoleName(roleSummary.getRoleName());
                roleRankPo.setRoleJobId(roleSummary.getRoleJob());
            }
            fightScoreRank.add(roleRankPo);
        }
    }

    @Override
    public void updateRewardRecord(long roleId, ActRoleNsFightScore po) {
        roleRewardRecord.put(roleId, po);
    }

    @Override
    public int canRankingReward(long roleId, int[] rankingLimit) {
        List<AbstractRankPo> subList;
        if (fightScoreRank.size() < rankingLimit[1]) {
            subList = fightScoreRank;
        } else {
            subList = fightScoreRank.subList(rankingLimit[0] - 1, rankingLimit[1]);
        }
        int ranking = rankingLimit[0];
        for (AbstractRankPo po : subList) {
            RoleRankPo roleRankPo = (RoleRankPo) po;
            if (roleRankPo.getRoleId() == roleId)
                return ranking;
            ranking++;
        }
        return -1;
    }

    @Override
    public void sendHistoryRank(long roleId) {
        // send to client
        ClientNSFightScore packet = new ClientNSFightScore(ClientNSFightScore.HISTRY_RANK);
        packet.setHistoryRank(fightScoreRank);
        PlayerUtil.send(roleId, packet);
    }

    /**
     * 活动结束,邮件补发达标&未领取的奖励
     *
     * @param activityId
     */
    private void compensateReward(int activityId) {
        Map<Integer, NewServerFightScoreVo> voMap = NewServerFightManager.getNSFSVoMap(activityId);
        if (voMap == null)
            return;
        for (ActRoleNsFightScore roleNsFightScore : roleRewardRecord.values()) {
            Map<Integer, Integer> fightScoreReward = new HashMap<>();// 战力值奖励
            Map<Integer, Integer> rankingReward = new HashMap<>();// 战力排行奖励
            int ranking = -1;// 战力排名
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleNsFightScore.getRoleId(), SummaryConst.C_ROLE);
            if (rsc == null || rsc.isDummy()) {
                continue;
            }
            for (NewServerFightScoreVo vo : voMap.values()) {
                // 已领取
                if (roleNsFightScore.isRewarded(vo.getRewardId())) {
                    continue;
                }
                // 是否满足开始时间
                OperateActVo operateActVo = OperateActivityManager.getOperateActVo(activityId);
//                ActOpenTime1 actOpenTime = (ActOpenTime1) operateActVo.getActOpenTimeBase();
//                int spendDays = DateUtil.getRelativeDifferDays(actOpenTime.getStartDate(), new Date()) + 1;

                ActOpenTime3 actOpenTime = (ActOpenTime3) operateActVo.getActOpenTimeBase();
                int spendDays = 0;
                if (actOpenTime != null) {
                    int openServerDays = DataManager.getServerDays();
                    spendDays = ActOpenTimeBase.getOpenDaysByOpenTime3(actOpenTime, openServerDays);
                }

                if (spendDays < vo.getDay()) {
                    continue;
                }
                switch (vo.getType()) {
                    case NewServerFightManager.REWARD_TYPE_FIGHTSCORE:// 战力值
                        if (rsc.getFightScore() >= vo.getMinFightScore()) {
                            Map<Integer, Integer> reward = DropUtil.executeDrop(vo.getReward(), 1);
                            MapUtil.add(fightScoreReward, reward);
                            roleNsFightScore.updateRewardRecord(vo.getRewardId());
                        }
                        break;
                    case NewServerFightManager.REWARD_TYPE_RANKING:// 战力排行
                        int check = canRankingReward(roleNsFightScore.getRoleId(), vo.getRankLimit());
                        if (check != -1) {
                            ranking = check;
                            Map<Integer, Integer> reward = DropUtil.executeDrop(vo.getReward(), 1);
                            MapUtil.add(rankingReward, reward);
                            roleNsFightScore.updateRewardRecord(vo.getRewardId());
                        }
                        break;
                }
            }
            try {
                if (!fightScoreReward.isEmpty()) {
                    ServiceHelper.emailService().sendToSingle(roleNsFightScore.getRoleId(),
                            NewServerFightManager.FIGHTSCORE_EMAIL_TEMPLATE, 0L, NewServerFightManager.SENDER_NAME,
                            fightScoreReward, String.valueOf(rsc.getFightScore()));
                }
                if (!rankingReward.isEmpty() && ranking != -1) {
                    ServiceHelper.emailService().sendToSingle(roleNsFightScore.getRoleId(),
                            NewServerFightManager.RANKING_EMAIL_TEMPLATE, 0L, "系统",
                            rankingReward, String.valueOf(ranking));
                }
            } catch (Exception e) {
                LogUtil.error("新服冲战力活动发奖异常,roleId={},补发战力值奖励={},排名奖励={}", fightScoreReward.toString(),
                        rankingReward.toString());
                LogUtil.error("调用邮件接口发奖异常", e);
                continue;
            }
        }
    }
}
