package com.stars.modules.baseteam.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/16.
 */
public class ClientBaseTeamApply extends PlayerPacket {
    private byte tag;// 子协议

    public static final byte APPLY_LIST = 0;// 申请列表
    public static final byte ADD_UPDATE_APPLY_MEMBER = 1;// 新增/更新申请人
    public static final byte REMOVE_APPLY = 2;// 移除申请
    public static final byte REMOVE_ALL_APPLY = 3;// 清空申请列表
    public static final byte TEAM_MEMBER_MAX = 4;// 返回通知客户端队伍人数已满
    public static final byte TEAM_NOT_EXIT = 5;// 返回通知客户端队伍已不存在
    public static final byte CAN_APPLY_TEAM = 6;// 可申请队伍列表

    /* 参数 */
    private Map<Long, BaseTeamMember> applyMembers;
    private BaseTeamMember applyMember;
    private long removeApplierId;// 申请者Id
    private List<BaseTeam> teamList;

    public ClientBaseTeamApply() {
    }

    public ClientBaseTeamApply(byte tag) {
        this.tag = tag;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BaseTeamPacketSet.Client_TeamApply;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(tag);
        switch (tag) {
            case APPLY_LIST:
                writeApplyList(buff);
                break;
            case ADD_UPDATE_APPLY_MEMBER:
                applyMember.writeToBuffer(buff);
                break;
            case REMOVE_APPLY:
                buff.writeString(String.valueOf(removeApplierId));
                break;
            case CAN_APPLY_TEAM:
                writeCanApplyTeam(buff);
                break;
        }
    }

    private void writeApplyList(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (applyMembers == null ? 0 : applyMembers.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (BaseTeamMember teamMember : applyMembers.values()) {
            teamMember.writeToBuffer(buff);
        }
    }

    private void writeCanApplyTeam(NewByteBuffer buff) {
        byte size = (byte) (teamList == null ? 0 : teamList.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        int currentTime = DateUtil.getCurrentTimeInt();
        for (BaseTeam team : teamList) {
            buff.writeInt(team.getTeamId());
            buff.writeByte(team.getTeamType());
            buff.writeInt(team.getTarget());
            buff.writeByte(team.getMemberCount());
            buff.writeByte(team.getMaxMemberCount());
            buff.writeByte(team.getMinMemberCount());
            BaseTeamMember captain = team.getCaptain();
            buff.writeString(String.valueOf(captain.getRoleId()));
            buff.writeString(captain.getName());
            buff.writeShort(captain.getLevel());
            buff.writeByte(captain.getJob());
//            buff.writeInt(currentTime-team.getCreateTime());
            
            //新需求，队员数据也要下发到客户端
            Map<Long, BaseTeamMember> members = new HashMap<Long, BaseTeamMember>(team.getMembers());
            members.remove(captain.getRoleId());//移除队长           
            byte mSize = (byte) (members.size());
            //buff.writeByte(mSize);
            if (mSize > 0) {
				for (BaseTeamMember member : members.values()) {
					buff.writeString(String.valueOf(member.getRoleId()));
		            buff.writeString(member.getName());
		            buff.writeShort(member.getLevel());
		            buff.writeByte(member.getJob());
				}
			}
        }
    }

    public void setApplyMembers(Map<Long, BaseTeamMember> applyMembers) {
        this.applyMembers = applyMembers;
    }

    public void setApplyMember(BaseTeamMember applyMember) {
        this.applyMember = applyMember;
    }

    public void setRemoveApplierId(long removeApplierId) {
        this.removeApplierId = removeApplierId;
    }

    public void setTeamList(List<BaseTeam> teamList) {
        this.teamList = teamList;
    }
}
