package com.stars.modules.baseteam.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.modules.baseteam.userdata.BaseTeamInvitor;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by liuyuheng on 2016/11/15.
 */
public class ClientBaseTeamInvite extends PlayerPacket {
    private byte tag;// 子协议

    public static final byte CAN_INVITE_LIST = 0;// 可邀请成员列表
    public static final byte RECEIVE_INVITE_LIST = 1;// 收到的邀请列表
    public static final byte NEW_RECEIVE_INVITE = 2;// 收到新的邀请
    public static final byte REMOVE_INVITE = 3;// 移除邀请
    public static final byte REMOVE_ALL_INVITE = 4;// 一键清除所有邀请

    /* 参数 */
    private List<TeamInvitee> invitees;
    private byte inviteeType;// 0周围玩家 1好友 2家族
    private List<BaseTeamInvitor> invitors;
    private BaseTeamInvitor invitor;
    private byte teamType;
    private long curTimes;

    public ClientBaseTeamInvite() {
    }

    public ClientBaseTeamInvite(byte tag) {
        this.tag = tag;
        switch (tag) {
            case CAN_INVITE_LIST:
                invitees = new ArrayList<>();
                break;
            case RECEIVE_INVITE_LIST:
                invitors = new ArrayList<>();
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Client_TeamInvite;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(tag);
        switch (tag) {
            case CAN_INVITE_LIST:// 可邀请成员列表
                writeCanInviteList(buff);
                break;
            case RECEIVE_INVITE_LIST:// 收到的邀请列表
                writeReceiveInviteList(buff);
                break;
            case NEW_RECEIVE_INVITE:// 收到新的邀请
                writeReceiveInvite(buff, invitor);
                break;
            case REMOVE_INVITE:// 移除邀请
                buff.writeString(String.valueOf(invitor.getInvitorId()));
                break;
        }
    }

    private void writeCanInviteList(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) invitees.size();
        buff.writeByte(inviteeType);// 0周围玩家 1好友 2家族
        buff.writeByte(size);
        if (size == 0)
            return;
        for (TeamInvitee teamInvitee : invitees) {
            buff.writeString(String.valueOf(teamInvitee.getId()));
            buff.writeString(teamInvitee.getName());
            buff.writeShort(teamInvitee.getLevel());
            buff.writeByte(teamInvitee.getJob());
            buff.writeInt(teamInvitee.getFightScore());
        }
    }

    private void writeReceiveInviteList(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (invitors == null ? 0 : invitors.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (BaseTeamInvitor teamInvitor : invitors) {
            writeReceiveInvite(buff, teamInvitor);
        }
        buff.writeLong(curTimes);
    }

    private void writeReceiveInvite(NewByteBuffer buff, BaseTeamInvitor teamInvitor) {
        buff.writeString(String.valueOf(teamInvitor.getInvitorId()));// roleId
        buff.writeString(teamInvitor.getName());// 名字
        buff.writeShort(teamInvitor.getLevel());// 等级
        buff.writeByte(teamInvitor.getJob());// 职业
        buff.writeInt(teamInvitor.getFightScore());// 战力
        buff.writeInt(teamInvitor.getTarget());// 队伍目标
        buff.writeByte(teamInvitor.getMemberCount());// 队伍当前人数
        buff.writeByte(teamInvitor.getMaxMemberCount());// 队伍最大人数
        buff.writeByte(teamInvitor.getTeamType());// 队伍类型
        buff.writeLong(curTimes);
    }

    public void addInvitee(TeamInvitee teamInvitee) {
        invitees.add(teamInvitee);
    }

    public void setInviteeType(byte inviteeType) {
        this.inviteeType = inviteeType;
    }

    public void setInvitors(Collection<BaseTeamInvitor> invitors) {
        this.invitors.addAll(invitors);
    }

    public void setInvitor(BaseTeamInvitor invitor) {
        this.invitor = invitor;
    }

    public void setTeamType(byte teamType) {
        this.teamType = teamType;
    }

    public long getCurTimes() {
        return curTimes;
    }

    public void setCurTimes(long curTimes) {
        this.curTimes = curTimes;
    }
}
