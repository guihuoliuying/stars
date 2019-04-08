package com.stars.multiserver.teamPVPGame;

import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.modules.teampvpgame.packet.ClientTPGScoreRank;
import com.stars.modules.teampvpgame.prodata.DoublePVPRewardVo;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TPGLocalServiceActor extends ServiceActor implements TPGLocalService {
    private TPGHost tpgHost;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.TPGLocalService, this);
        synchronized (TPGLocalServiceActor.class) {
            String sql = "select max(tpgid) from `teampvpgame`; ";
            Integer maxTpgId = DBUtil.queryBean(DBUtil.DB_USER, Integer.class, sql);
            tpgHost = new TPGHost(String.valueOf(maxTpgId), TPGUtil.TPG_LOACAL, TPGUtil.localTPGFlow, DBUtil.DB_USER);
        }
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.TPG,new Runnable() {
            @Override
            public void run() {
                ServiceHelper.tpgLocalService().maintenance();
                ServiceHelper.tpgLocalService().save();
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {

    }

    @Override
    public void onReceived0(Object message, Actor sender) {
        tpgHost.onReceived(message);
    }

    @Override
    public boolean isStepTimeActive() {
        return tpgHost.isStepTimeActive();
    }

    @Override
    public boolean isInStep(String stepType) {
        return tpgHost.isInStep(stepType);
    }

    @Override
    public boolean canSignUp(long inititor) {
        return tpgHost.canSignUp(inititor);
    }

    @Override
    public void signUp(int teamId) {
        tpgHost.signUp(teamId);
    }

    @Override
    public void updateTPGTeamMember(BaseTeamMember teamMember) {
        tpgHost.updateTPGTeamMember(teamMember);
    }

    @Override
    public void reqCurTPGData(long initiator) {
        tpgHost.sendTPGData(initiator, TPGUtil.TPG_LOACAL);
    }

    @Override
    public void reqScoreMatchResult(long initiator) {
        tpgHost.reqScoreMatchResult(initiator);
    }

    @Override
    public void enterFight(long initiator) {
        tpgHost.enterFight(initiator);
    }

    @Override
    public void doFightLuaFram(int serverId, String fightSceneId, LuaFrameData luaFrameData) {
        tpgHost.doFightLuaFram(fightSceneId, luaFrameData);
    }

    @Override
    public void reqScoreRank(long initiator) {
        tpgHost.reqScoreRank(initiator);
    }

    @Override
    public void reqScoreRanking(long initiator, List<Integer> teamIdList) {
        Map<Integer, Integer> rankings = new HashMap<>();
        for (int teamId : teamIdList) {
            rankings.put(teamId, tpgHost.getScoreRanking(teamId));
        }
        ClientTPGScoreRank clientTPGScoreRank = new ClientTPGScoreRank(ClientTPGScoreRank.SCORE_RANKING_BYTEAMID);
        clientTPGScoreRank.setRankingMap(rankings);
        PacketManager.send(initiator, clientTPGScoreRank);
    }

    @Override
    public void grantReward(Collection<TPGTeam> teams, int awardType, boolean hasParam) {
        // 发奖
        int rank = 1;
        for (TPGTeam tpgTeam : teams) {
            DoublePVPRewardVo rewardVo = TeamPVPGameManager.getTPGReward(awardType, rank);
            if (rewardVo != null) {
                for (long roleId : tpgTeam.getMembers().keySet()) {
                    // 是否需要替换参数
                    if (hasParam) {
                        ServiceHelper.emailService().sendToSingle(roleId, rewardVo.getEmailTemplate(), 0L,
                                TPGUtil.rewardEmailSenderName, rewardVo.getRewardMap(), String.valueOf(tpgTeam.getScore()),
                                String.valueOf(rank));
                    } else {
                        ServiceHelper.emailService().sendToSingle(roleId, rewardVo.getEmailTemplate(), 0L,
                                TPGUtil.rewardEmailSenderName, rewardVo.getRewardMap());
                    }
                }
            }
            rank++;
        }
    }

    @Override
    public void maintenance() {
        tpgHost.maintenance();
    }

    @Override
    public void save() {
        tpgHost.saveToDb();
    }
}
