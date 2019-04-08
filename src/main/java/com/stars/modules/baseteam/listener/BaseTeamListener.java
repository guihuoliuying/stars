package com.stars.modules.baseteam.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.baseteam.event.BaseTeamEvent;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;

/**
 * Created by liuyuheng on 2016/11/10.
 */
public class BaseTeamListener extends AbstractEventListener<BaseTeamModule> {
    public BaseTeamListener(BaseTeamModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (module().isSpecialAccount()) {
            return;
        }
        if (event instanceof BaseTeamEvent) {
            BaseTeamEvent bte = (BaseTeamEvent) event;
            byte tag = bte.getTag();
            if (tag == BaseTeamEvent.RECEIVE_INVITE) {// 收到邀请
                module().receiveInvite(bte.getTeamInvitor());
            } else if (tag == BaseTeamEvent.JOIN_TEAM) {// 加入队伍
                module().joinTeamHandler(bte.getTeamType());
            } else if (tag == BaseTeamEvent.CANCEL_MATCH_MEMBER) {// 取消匹配队员
                module().reqCancelMatchMember(bte.getNotice());
            } else if (tag == BaseTeamEvent.CANCEL_MATCH_TEAM) {// 取消匹配队伍
                module().reqCancelMatchTeam(false);
            } else if (tag == BaseTeamEvent.APPLY_JOIN_TEAM) {// 收到入队申请
                module().recieveApplyJoinTeam(bte.getApplierId());
            }
            if (bte.getTag() == BaseTeamEvent.TEAM_TARGET_CHANGED) {// 队伍目标已更换
                module().targetChangedHandler(bte.getTeamType(), bte.getTeamTarget());
            }
            if (bte.getTag() == BaseTeamEvent.DELETE_INVITE) {// 清除邀请信息
                module().handleDeleteInvitEvent(bte);
            }
        } else if (event instanceof FightScoreChangeEvent || event instanceof SkillPositionChangeEvent
                || event instanceof DeityWeaponChangeEvent) {
            /**
             * 更新队员属性
             * 条件:1.角色战力改变(属性改变) 2.携带技能改变 3.当前神兵改变
             */
            module().updateTeamMember();
        }
    }
}
