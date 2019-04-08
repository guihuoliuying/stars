package com.stars.modules.baseteam.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/11/15.
 */
public class ClientBaseTeamInfo extends PlayerPacket {
    private byte tag;

    public static final byte TEAMINFO = 0;// 队伍信息
    public static final byte ADD_UPDATE_TEAMMEMBER = 1;// 添加/更新队伍成员
    public static final byte REMOVE_TEAMMEMBER = 2;// 移除队伍成员
    public static final byte CHANGE_CAPTAIN = 3;// 变更队长
    public static final byte LOST_TEAM = 4;// 失去队伍(踢出队伍或者队伍解散)
    public static final byte SET_OPEN_APPLY = 5;// 设置开放申请标志
    public static final byte CHANGE_TEAM_TARGET = 6;// 该变更队伍目标

    /* 参数 */
    private BaseTeam team;// 队伍信息
    private List<BaseTeamMember> teamMembers;// 队伍成员
    private List<Long> removeRoleIds;// 移除成员roleId
    private long newCaptain;// 新队长
    private byte openApply;// 开放申请标志
    private int teamTarget;// 队伍目标

    public ClientBaseTeamInfo() {
    }

    public ClientBaseTeamInfo(byte tag) {
        this.tag = tag;
        if (tag == TEAMINFO || tag == ADD_UPDATE_TEAMMEMBER) {
            teamMembers = new LinkedList<>();
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Client_TeamInfo;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(tag);
        switch (tag) {
            case TEAMINFO:
                writeTeamInfo(buff);
                break;
            case ADD_UPDATE_TEAMMEMBER:
                writeTeamMember(buff);
                break;
            case REMOVE_TEAMMEMBER:
                byte size = (byte) (removeRoleIds == null ? 0 : removeRoleIds.size());
                buff.writeByte(size);
                if (size == 0)
                    return;
                for (long id : removeRoleIds) {
                    buff.writeString(String.valueOf(id));
                }
                break;
            case CHANGE_CAPTAIN:
                buff.writeString(String.valueOf(newCaptain));
                break;
            case SET_OPEN_APPLY:
                buff.writeByte(openApply);
                break;
            case CHANGE_TEAM_TARGET:
                buff.writeInt(teamTarget);
                break;
        }
    }

    private void writeTeamInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(team.getTeamId());
        buff.writeByte(team.getTeamType());
        buff.writeInt(team.getTarget());
        buff.writeByte((byte) (team.isOpenApply() ? 1 : 0));
        buff.writeString(String.valueOf(team.getCaptainId()));
        buff.writeByte(team.getMemberCount());
        buff.writeByte(team.getMinMemberCount());
        buff.writeByte(team.getMaxMemberCount());
//        buff.writeInt(DateUtil.getCurrentTimeInt()-team.getCreateTime());
        if (team.getMemberCount() == 0)
            return;
        for (BaseTeamMember teamMember : team.getMembers().values()) {
            teamMember.writeToBuffer(buff);
        }
    }

    private void writeTeamMember(NewByteBuffer buff) {
        byte size = (byte) (teamMembers == null ? 0 : teamMembers.size());
        buff.writeByte(size);
        for (BaseTeamMember teamMember : teamMembers) {
            teamMember.writeToBuffer(buff);
        }
    }

    public void addMember(BaseTeamMember tm) {
        teamMembers.add(tm);
    }

    public void setTeam(BaseTeam team) {
        this.team = team;
    }

    public void setRemoveRoleIds(List<Long> removeRoleIds) {
        this.removeRoleIds = removeRoleIds;
    }

    public void setNewCaptain(long newCaptain) {
        this.newCaptain = newCaptain;
    }

    public void setOpenApply(byte openApply) {
        this.openApply = openApply;
    }

    public void setTeamTarget(int teamTarget) {
        this.teamTarget = teamTarget;
    }
}
