package com.stars.modules.camp.activity.imp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.activity.AbstractCampActivity;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.packet.ClientCampFightPacket;
import com.stars.modules.camp.pojo.CampFightGrowUP;
import com.stars.modules.camp.prodata.CampActivityVo;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipModule;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.services.ServiceHelper;
import com.stars.util.MapUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.stars.modules.camp.CampModule.REDPOINT_QI_CHU_DA_ZUO_ZHAN;

/**
 * Created by huwenjun on 2017/7/19.
 */
public class QiChuDaZuoZhanActivity extends AbstractCampActivity {
    public static final int dayFightScoreEmailTemplate = 32001;

    public QiChuDaZuoZhanActivity(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap, RoleCampPo roleCampPo, RoleCampTimesPo roleCampTimesPo) {
        super(name, id, self, eventDispatcher, moduleMap, roleCampPo, roleCampTimesPo);
    }

    @Override
    public void onTimingExecute() {
        CampActivityVo campActivityVo = CampManager.campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
        int qiChuDaZuoZhanFlag = getInt(REDPOINT_QI_CHU_DA_ZUO_ZHAN, 0);
        if (qiChuDaZuoZhanFlag == 0 && campActivityVo.checkOpenTime()) {
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        setInt(REDPOINT_QI_CHU_DA_ZUO_ZHAN, 0);
        int campFightScore = getRoleCampTimes().getCampFightScore();
        Map<Integer, Integer> reward = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : CampManager.campActivity2ScoreMap.entrySet()) {
            if (campFightScore >= entry.getKey() && getRoleCampTimes().canTakeScoreReward(entry.getKey())) {
                getRoleCampTimes().takeScoreReward(entry.getKey());
                MapUtil.add(reward, entry.getValue());
            }
        }
        if (!reward.isEmpty()) {
            ServiceHelper.emailService().sendToSingle(id(), dayFightScoreEmailTemplate, 0L, "系统", reward);
        }
        context().update(getRoleCampTimes());
    }

    /**
     * 开始匹配
     */
    public void startMatching() {
        CampActivityVo campActivityVo = CampManager.campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
        if ((!campActivityVo.isOpen()) || (!campActivityVo.checkOpenTime())) {
            warn("campactivity2_tips_opentime");
            return;
        }
        CampFightMatchInfo campFightMatchInfo = new CampFightMatchInfo();
        campFightMatchInfo.setRoleId(id());
        campFightMatchInfo.setCampType(getRoleCamp().getCampType());
        campFightMatchInfo.setFromServerId(MultiServerHelper.getServerId());
        campFightMatchInfo.setTakeSingleRewardTime(getRoleCampTimes().getTakeSingleRewardTime());
        FighterEntity self = FighterCreator.createPlayer(moduleMap());
        int campType = getRoleCamp().getCampType();
        self.setCamp((byte) campType);
        campFightMatchInfo.setCampFightEntity(self);
        CampFightGrowUP campFightGrowUP = new CampFightGrowUP();
        campFightGrowUP.setFightUid(id() + "");
        campFightGrowUP.setLevel(1);
        campFightGrowUP.setCommonOfficerId(getRoleCamp().getCommonOfficerId());
        campFightGrowUP.setRareOfficerId(getRoleCamp().getRareOfficerId());
        VipModule vipModule = module(MConst.Vip);
        RoleModule roleModule = module(MConst.Role);
        campFightGrowUP.setName(roleModule.getRoleRow().getName());
        campFightGrowUP.setVipLevel(vipModule.getVipLevel());
        campFightGrowUP.setServerName(MultiServerHelper.getServerName());
        campFightMatchInfo.setCampFightGrowUP(campFightGrowUP);
        ServiceHelper.campLocalFightService().startMatching(campFightMatchInfo);
        warn("campactivity2_tips_macthtips");
    }

    /**
     * 取消匹配
     */
    public void cancelMatching() {
        CampFightMatchInfo campFightMatchInfo = new CampFightMatchInfo();
        campFightMatchInfo.setRoleId(id());
        campFightMatchInfo.setCampType(getRoleCamp().getCampType());
        campFightMatchInfo.setFromServerId(MultiServerHelper.getServerId());
        ServiceHelper.campLocalFightService().cancelMatching(campFightMatchInfo);
    }

    /**
     * 请求打开主界面数据
     */
    public void reqMainUIDate() {
        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN);
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_FIGHT_MAIN_UI_DATA);
        clientCampFightPacket.setRoleCampTimesPo(getRoleCampTimes());
        send(clientCampFightPacket);
    }

    /**
     * 齐楚大作战积分奖励领取
     *
     * @param score
     */
    public void takeScoreReward(int score) {
        if (getRoleCampTimes().canTakeScoreReward(score)) {
            getRoleCampTimes().takeScoreReward(score);
            context().update(getRoleCampTimes());
            Map<Integer, Integer> reward = CampManager.campActivity2ScoreMap.get(score);
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(reward, EventType.CAMP_DAILY_SCORE_REWARD.getCode());
            ClientAward clientAward = new ClientAward(reward);
            send(clientAward);
            reqMainUIDate();
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD);
        } else {
            warn("已经领取");
        }
    }

    /**
     * 增加战力积分
     *
     * @param score
     */
    public void updateCampFightScore(int score) {
        getRoleCampTimes().addCampFightScore(score);
        context().update(getRoleCampTimes());
        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD);
    }
}
