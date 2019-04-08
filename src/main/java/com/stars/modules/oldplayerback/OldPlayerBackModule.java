package com.stars.modules.oldplayerback;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.drop.DropModule;
import com.stars.modules.oldplayerback.packet.ClientOldPalyerBackPacket;
import com.stars.modules.oldplayerback.usrdata.OldPlayerRewardPo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 1:表示本角色正在活动中，开启活动界面
 * Created by huwenjun on 2017/7/13.
 */
public class OldPlayerBackModule extends AbstractModule implements AccountRowAware {
    private AccountRow accountRole = null;
    OldPlayerRewardPo oldPlayerRewardPo;
    private int state = 0;

    public OldPlayerBackModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String account = accountRole.getName();
        String sql = "select * from oldplayerreward where account='%s';";
        oldPlayerRewardPo = DBUtil.queryBean(DBUtil.DB_USER, OldPlayerRewardPo.class, String.format(sql, account));
    }


    @Override
    public void onInit(boolean isCreation) throws Throwable {
        /**
         * 在活动时间内才进行状态检测
         */
        if (OldPlayerBackManager.ComebackRewardActTime.inActivityTime(new Date())) {
            state = checkState();
            if (state == 1) {
                signCalRedPoint(MConst.OldPlayerBack, RedPointConst.OLD_PLAYER_BACK);
            }
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (state == 1) {
            signCalRedPoint(MConst.OldPlayerBack, RedPointConst.OLD_PLAYER_BACK);
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.OLD_PLAYER_BACK)) {
            if (oldPlayerRewardPo != null && oldPlayerRewardPo.getState() == 1) {
                if (oldPlayerRewardPo.checkTime()) {
                    redPointMap.put(RedPointConst.OLD_PLAYER_BACK, "");
                } else {
                    redPointMap.put(RedPointConst.OLD_PLAYER_BACK, null);
                }
            }else {
                redPointMap.put(RedPointConst.OLD_PLAYER_BACK, null);
            }
        }
    }

    @Override
    public void onTimingExecute() {
        if (DateUtil.getSecondsBetweenTwoDates(new Date(), OldPlayerBackManager.ComebackRewardActTime.getEndDate()) == 0) {
            ClientOldPalyerBackPacket clientOldPalyerBackPacket = new ClientOldPalyerBackPacket(ClientOldPalyerBackPacket.SEND_ACTIVITY_FINISH);
            send(clientOldPalyerBackPacket);
            state = 0;
        }
    }

    /**
     * 0:全角色条件不符合
     * 1:表示满足条件
     * 检测角色是否满足条件
     */
    public int checkState() {
        if (oldPlayerRewardPo != null) {
            if (oldPlayerRewardPo.getRoleId() == id()) {
                /**
                 * 本角色正在活动中
                 */
                return oldPlayerRewardPo.getState();
            }
        } else {
            Map<Long, LoginRow> loginRowMap = accountRole.getLoginRowMap();
            List<AccountRole> relativeRoleList = accountRole.getRelativeRoleList();
            if (loginRowMap == null || relativeRoleList == null) {
                return 0;
            }
            boolean condition1 = true;
            boolean condition2 = true;
            /**
             * 全角色条件检测
             */
            for (AccountRole accountRole : relativeRoleList) {
                String roleIdStr = accountRole.getRoleId();
                long roleId = Long.parseLong(roleIdStr);
                LoginRow loginRow = loginRowMap.get(roleId);
                long lastLoginTimestamp = loginRow.getLastLoginTimestamp();
                if (accountRole.getRoleId().equals(id() + "")) {
                    if (accountRole.roleLevel < OldPlayerBackManager.ComebackLoginRoleimitLevel) {
                        com.stars.util.LogUtil.info("老玩家回归,登陆角色不满足开启条件:{}", id());
                        condition2 = false;
                    }
                    lastLoginTimestamp = loginRow.getLastLastLoginTimestamp();
                }
                if (accountRole.roleLevel >= OldPlayerBackManager.ComebackAllRoleLimitConf.getLevel()) {
                    if (!OldPlayerBackManager.ComebackAllRoleLimitConf.check(lastLoginTimestamp)) {
                        com.stars.util.LogUtil.info("老玩家回归,账号下所有角色不满足开启条件:{}", id());
                        condition1 = false;
                    }
                }
            }
            if (condition1 && condition2) {
                LogUtil.info("老玩家回归,条件满足，老玩家你好:{}", id());
                oldPlayerRewardPo = new OldPlayerRewardPo(accountRole.getName(), id(), 1);
                context().insert(oldPlayerRewardPo);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRole = accountRow;
    }

    /**
     * 领奖
     */
    public void takeReward() {
        DropModule dropModule = module(MConst.Drop);
        ToolModule toolModule = module(MConst.Tool);
        if (state == 1) {
            if (oldPlayerRewardPo.checkTime()) {
                int day = this.oldPlayerRewardPo.takeReward();
                if (day != 0) {
                    Integer groupId = OldPlayerBackManager.dayReward.get(day).getGroupId();
                    Map<Integer, Integer> reward = dropModule.executeDrop(groupId, 1, false);
                    toolModule.addAndSend(reward, EventType.OLD_PLAYER_BACK.getCode());
                    ClientOldPalyerBackPacket clientOldPalyerBackPacket = new ClientOldPalyerBackPacket(ClientOldPalyerBackPacket.SEND_REWARD_POSITION);
                    clientOldPalyerBackPacket.setPosition(day);
                    clientOldPalyerBackPacket.setReward(reward);
                    send(clientOldPalyerBackPacket);
                    ServerLogModule serverLogModule = module(MConst.ServerLog);
                    serverLogModule.log_old_player_back_reward(day, reward);
                    signCalRedPoint(MConst.OldPlayerBack, RedPointConst.OLD_PLAYER_BACK);
                } else {
                    warn("comeback_reward_prompt3");
                }
                if (oldPlayerRewardPo.getNextTakeRewardPosition() == 0) {
                    oldPlayerRewardPo.setState(0);
                    state = 0;
                }
                context().update(oldPlayerRewardPo);
            } else {
                warn("comeback_reward_prompt2");
            }
        } else {
            if (oldPlayerRewardPo.getNextTakeRewardPosition() == 0) {
                warn("comeback_reward_prompt3");
            } else {
                warn("comeback_reward_prompt1");
            }
        }
    }

    /**
     * 请求奖励展示
     */
    public void reqRewardShow() {
        if (oldPlayerRewardPo == null) {
            warn("活动数据初始化失败，请检查是否满足开启条件");
            return;
        }
        ClientOldPalyerBackPacket clientOldPalyerBackPacket = new ClientOldPalyerBackPacket(ClientOldPalyerBackPacket.SEND_REWARD_SHOW);
        clientOldPalyerBackPacket.setOldPlayerRewardPo(oldPlayerRewardPo);
        clientOldPalyerBackPacket.setRewardMap(OldPlayerBackManager.dayReward);
        clientOldPalyerBackPacket.setActivityDate(OldPlayerBackManager.ComebackRewardActTimeStr);
        send(clientOldPalyerBackPacket);

    }

    public int getState() {
        return state;
    }
}
