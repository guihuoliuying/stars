package com.stars.services.baseteam;

import com.stars.util.DateUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/11/7.
 */
public class BaseTeam {
    protected int teamId;// 队伍Id
    protected byte teamType;// 区分不同队伍类型
    protected Map<Long, BaseTeamMember> memberMap;// 队员
    protected Map<Long, BaseTeamMember> applyMembers;// 申请列表
    protected Set<Long> invitedMembers;// 邀请列表
    protected byte minMemberCount;// 队伍最小人数
    protected byte maxMemberCount;// 队伍最大人数
    protected long captainId;// 队长roleId
    protected boolean openApply;// 开放申请
    protected boolean isFight;// 是否在战斗中
    protected int target;// 队伍目标(部分组队类型需要,如:日常组队)
    protected int createTime;//创建队伍时间

    public BaseTeam(int teamId, byte teamType, byte minMemberCount, byte maxMemberCount, BaseTeamMember creator, int target) {
        this.teamId = teamId;
        this.teamType = teamType;
        this.minMemberCount = minMemberCount;
        this.maxMemberCount = maxMemberCount;
        this.target = target;
        this.captainId = creator.getRoleId();
        this.openApply = true;
        this.isFight = false;
        this.memberMap = new HashMap<>();
        this.applyMembers = new HashMap<>();
        this.invitedMembers = new HashSet<>();
        this.createTime = DateUtil.getCurrentTimeInt();
        addUpdateMember(creator);
    }

    /**
     * 新增/更新队员
     *
     * @param member
     */
    public void addUpdateMember(BaseTeamMember member) {
        this.memberMap.put(member.getRoleId(), member);
    }

    /**
     * 新增申请者
     *
     * @param invitee
     */
    public void addApplyMember(BaseTeamMember invitee) {
        this.applyMembers.put(invitee.getRoleId(), invitee);
    }

    /**
     * 移除队员
     *
     * @param member
     * @return
     */
    public BaseTeamMember removeMember(long member) {
        return this.memberMap.remove(member);
    }

    /**
     * 移除申请者
     *
     * @param target
     * @return
     */
    public BaseTeamMember removeApplyMember(long target) {
        return this.applyMembers.remove(target);
    }

    /**
     * 当前队伍人数
     *
     * @return
     */
    public byte getMemberCount() {
        return (byte) memberMap.size();
    }

    /**
     * 队伍真实玩家数量
     *
     * @return
     */
    public byte getPlayerMemberCount() {
        byte count = 0;
        for (BaseTeamMember teamMember : memberMap.values()) {
            if (teamMember.isPlayer()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获得真实队员
     *
     * @return
     */
    public Map<Long, BaseTeamMember> getPlayerMembers() {
        Map<Long, BaseTeamMember> map = new HashMap<>();
        for (BaseTeamMember teamMember : memberMap.values()) {
            if (teamMember.isPlayer()) {
                map.put(teamMember.getRoleId(), teamMember);
            }
        }
        return map;
    }

    /**
     * 获得真实玩家总战力
     */
    public int getPlayerTotalFighting() {
        int fighting = 0;
        for (BaseTeamMember teamMember : memberMap.values()) {
            if (teamMember.isPlayer()) {
                fighting += teamMember.getRoleEntity().getFightScore();
            }
        }
        return fighting;
    }
    
    /**
     * 获得真实队员
     *
     * @return
     */
    public Map<Long, BaseTeamMember> getRobotMembers() {
        Map<Long, BaseTeamMember> map = new HashMap<>();
        for (BaseTeamMember teamMember : memberMap.values()) {
            if (!teamMember.isPlayer()) {
                map.put(teamMember.getRoleId(), teamMember);
            }
        }
        return map;
    }

    /**
     * 获得所有队员
     *
     * @return
     */
    public Map<Long, BaseTeamMember> getMembers() {
        return memberMap;
    }

    /**
     * 获得指定队员
     *
     * @param roleId
     * @return
     */
    public BaseTeamMember getMember(long roleId) {
        return memberMap.get(roleId);
    }

    /**
     * 获得所有申请者
     *
     * @return
     */
    public Map<Long, BaseTeamMember> getApplyMembers() {
        return applyMembers;
    }

    /**
     * 获得队长
     *
     * @return
     */
    public BaseTeamMember getCaptain() {
        return memberMap.get(captainId);
    }

    /**
     * 是否在队伍内
     *
     * @param uniqueId
     * @return
     */
    public boolean isMember(long uniqueId) {
        return memberMap.containsKey(uniqueId);
    }

    /**
     * 是否在申请列表中
     *
     * @param roleId
     * @return
     */
    public boolean isApplyMember(long roleId) {
        return applyMembers.containsKey(roleId);
    }

    /**
     * 是否是真实玩家
     *
     * @param roleId
     * @return
     */
    public boolean isPlayer(long roleId) {
        if (!memberMap.containsKey(roleId))
            return false;
        return memberMap.get(roleId).isPlayer();
    }

    /**
     * 队伍能否被匹配到
     *
     * @return
     */
    protected boolean canMatch() {
        // 在战斗中
        if (isFight())
            return false;
        // 人数已满
        if (isFull())
            return false;
        if (!openApply)
            return false;
        return true;
    }

    /**
     * 是否战斗中
     *
     * @return
     */
    public boolean isFight() {
        return isFight;
    }

    /**
     * 人数已满
     *
     * @return
     */
    public boolean isFull() {
        return memberMap.size() >= maxMemberCount;
    }

    /**
     * 开放申请
     *
     * @return
     */
    public boolean isOpenApply() {
        return openApply;
    }

    /**
     * 自动更换队长
     * 队长给等级最高的玩家
     */
    public void autoChangeHeader() {
        Collection<BaseTeamMember> col = memberMap.values();
        long newHeader = -1;
        short maxLevel = -1;
        for (BaseTeamMember teamMember : col) {
            if (teamMember.getType() == 1)
                continue;
            if (teamMember.getLevel() <= maxLevel) {
                continue;
            }
            maxLevel = teamMember.getLevel();
            newHeader = teamMember.getRoleId();
        }
        this.captainId = newHeader;
    }

    /**
     * 清除所有申请者
     *
     * @return
     */
    public int clearAllApplyMember() {
        int size = this.applyMembers.size();
        this.applyMembers.clear();
        return size;
    }

    /**
     * 获得队员平均等级
     *
     * @return
     */
    public int getAverageLevel() {
        int levelSum = 0;
        for (BaseTeamMember member : memberMap.values()) {
            levelSum = levelSum + member.getLevel();
        }
        return levelSum / memberMap.size();
    }

    public int getTeamId() {
        return teamId;
    }

    public long getCaptainId() {
        return captainId;
    }

    public byte getTeamType() {
        return teamType;
    }

    public byte getMaxMemberCount() {
        return maxMemberCount;
    }

    public byte getMinMemberCount() {
        return minMemberCount;
    }

    public int getTarget() {
        return target;
    }

    public void setCaptainId(long captainId) {
        this.captainId = captainId;
    }

    public void setFight(boolean isFight) {
        this.isFight = isFight;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setOpenApply(boolean openApply) {
        this.openApply = openApply;
    }

    public Set<Long> getInvitedMembers() {
        return invitedMembers;
    }

    public void setInvitedMembers(Set<Long> invitedMembers) {
        this.invitedMembers = invitedMembers;
    }

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

    @Override
    public String toString() {
        return "BaseTeam{" +
                "teamId=" + teamId +
                ", teamType=" + teamType +
                ", memberMap=" + memberMap +
                ", applyMembers=" + applyMembers +
                ", invitedMembers=" + invitedMembers +
                ", minMemberCount=" + minMemberCount +
                ", maxMemberCount=" + maxMemberCount +
                ", captainId=" + captainId +
                ", openApply=" + openApply +
                ", isFight=" + isFight +
                ", target=" + target +
                ", createTime=" + createTime +
                '}';
    }
}
