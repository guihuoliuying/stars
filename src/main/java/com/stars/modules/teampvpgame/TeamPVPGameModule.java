package com.stars.modules.teampvpgame;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.teampvpgame.event.SignUpEvent;
import com.stars.modules.teampvpgame.packet.ClientTPGSignUp;
import com.stars.modules.teampvpgame.userdata.SignUpSubmiter;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/15.
 */
public class TeamPVPGameModule extends AbstractModule {

    /* 内存数据 */
    private SignUpSubmiter signUpSubmiter;// 报名提交者

    public TeamPVPGameModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("比武大会", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        signUpSubmiter = null;
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    @Override
    public void onTimingExecute() {
        // 确认报名时间超时
        if (signUpSubmiter != null &&
                (System.currentTimeMillis() - signUpSubmiter.getSubmitTimestamp()) > TeamPVPGameManager.signUpConfirmTime) {
            signUpSubmiter = null;
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
            if (team == null)
                return;
            // 退队处理
            ServiceHelper.baseTeamService().leaveTeam(id());
        }
    }

    /**
     * 提交报名,通知队员确认
     */
    public void submitSignUp() {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        // 没有队伍 || 队伍人数未满
        if (team == null || !team.isFull())
            return;
        SignUpSubmiter signUpSubmiter = new SignUpSubmiter(id());
        RoleModule roleModule = module(MConst.Role);
        signUpSubmiter.setRoleName(roleModule.getRoleRow().getName());
        signUpSubmiter.setJobId(roleModule.getRoleRow().getJobId());
        signUpSubmiter.setLevel(roleModule.getLevel());
        signUpSubmiter.setFightScore(roleModule.getRoleRow().getFightScore());
        // 提交时间戳
        long timestamp = System.currentTimeMillis();
        signUpSubmiter.setSubmitTimestamp(timestamp);
        for (long roleId : team.getPlayerMembers().keySet()) {
            // 发送给自己
            if (roleId == id()) {
                // send to client,
                ClientTPGSignUp clientTPGSignUp = new ClientTPGSignUp(ClientTPGSignUp.SUBMIT_SIGNUP);
                clientTPGSignUp.setResetConfirmTime((int) (TeamPVPGameManager.signUpConfirmTime -
                        Math.floor((System.currentTimeMillis() - signUpSubmiter.getSubmitTimestamp()) / 1000.0)));
                send(clientTPGSignUp);
                continue;
            }
            // 通知队友
            SignUpEvent event = new SignUpEvent(signUpSubmiter);
            ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 收到报名确认
     *
     * @param signUpSubmiter
     */
    public void receiveSignUp(SignUpSubmiter signUpSubmiter) {
        this.signUpSubmiter = signUpSubmiter;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        // 没有队伍 || 队伍人数未满
        if (team == null || !team.isFull())
            return;
        // send to client,
        ClientTPGSignUp clientTPGSignUp = new ClientTPGSignUp(ClientTPGSignUp.RECEIVE_SIGNUP_CONFIRM);
        clientTPGSignUp.setResetConfirmTime((int) (TeamPVPGameManager.signUpConfirmTime -
                Math.floor((System.currentTimeMillis() - signUpSubmiter.getSubmitTimestamp()) / 1000.0)));
        send(clientTPGSignUp);
    }

    /**
     * 同意报名
     */
    public void permitSignUp() {
        signUpSubmiter = null;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        // 没有队伍 || 队伍人数未满
        if (team == null || !team.isFull())
            return;
        // 向活动提交报名
        ServiceHelper.tpgLocalService().signUp(team.getTeamId());
    }

    /**
     * 拒绝报名
     */
    public void refuseSignUp() {
        signUpSubmiter = null;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        // 没有队伍 || 队伍人数未满
        if (team == null || !team.isFull())
            return;
        // 离队处理
        ServiceHelper.baseTeamService().leaveTeam(id());
    }

    /**
     * 更新队员数据
     */
    public void updateTPGTeamMember() {
        BaseTeamModule baseTeamModule = module(MConst.Team);
        ServiceHelper.tpgLocalService().updateTPGTeamMember(baseTeamModule.selfToTeamMember(BaseTeamManager.TEAM_TYPE_COUPLE_PVP));
    }

    public boolean isSpecialAccount() {
        return SpecialAccountManager.isSpecialAccount(id());
    }
}
