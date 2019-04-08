package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/8.
 */
public interface TeamHandler {
    /**
     * 创建队伍(需要业务判断)
     *
     * @param moduleMap
     * @param creator
     * @param teamType
     * @param target
     */
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target);

    /**
     * 可邀请成员列表
     *
     * @param moduleMap
     * @param initiator
     */
    public void sendCanInviteList(Map<String, Module> moduleMap, long initiator);

    /**
     * 能否改变队伍目标(业务判断)
     *
     * @param moduleMap
     * @param initiator
     * @param target
     * @return
     */
    public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target);
    
    /**
     * 是否所有队员都能改变队伍目标(业务判断)
     *
     * @param moduleMap
     * @param initiator
     * @param target
     * @return
     */
    public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target);

    /**
     * 更改队伍目标
     *
     * @param moduleMap
     * @param initiator
     * @param newTeamTarget
     */
    public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int newTeamTarget);

    /**
     * 自己能否加入队伍(业务判断,不能的话要在这里发送提示)
     *
     * @param moduleMap
     * @param initiator
     * @param team TODO
     * @param teamId
     * @return
     */
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int targetId, BaseTeam team);

    /**
     * 别人能否加入队伍(交给业务判断)
     *
     * @param target
     * @param teamTarget
     * @return
     */
    public boolean otherJoinInTeam(long target, int teamTarget);
    
    /**
     * 能否接受邀请
     *
     * @param 
     * @param teamTarget
     * @return
     */
    public boolean canBeInvite(long invitorId , long inviteedId, int teamTarget);
    
    /**
     * 是否可以继续匹配
     * @param match
     * @return
     */
    public boolean isCanMatch(BaseTeamMatch match);
    
    /**
     * 新建匹配对象处理
     * @param match
     * @param team TODO
     */
    public void newMatchHandle(BaseTeamMatch match, BaseTeam team);

    /**
     * 匹配队员(假玩家数据)
     *
     * @param captain
     * @param count
     * @param match TODO
     * @param team TODO
     * @return
     */
    public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception, BaseTeamMatch match, BaseTeam team);

    /**
     * 匹配队伍(假玩家数据)
     *
     * @param creator
     * @param teamType
     * @param target
     * @return
     */
    public boolean matchTeamWithFakePlayer(BaseTeamMember creator, byte teamType, int target);

    /**
     * 加入队伍后可能的操作
     *
     * @param roleId
     */
    public void afterJoinTeam(Map<String, Module> moduleMap, long roleId);

    /**
     * 打开组队界面处理
     */
    public void handleOpenTeamUI(long roleId,Map<String, Module> moduleMap);

    /**
     * 关闭组队界面处理
     */
    public void handleCloseTeamUI(long roleId);
    
    /**
     * 离开队伍
     */
    public void leaveTeam(long roleId);
    
    /**
     * 组队成员对象处理
     * @param tm TODO
     * @param moduleMap TODO
     */
    public BaseTeamMember createBaseTeamMember(Map<String, Module> moduleMap);
    
}
