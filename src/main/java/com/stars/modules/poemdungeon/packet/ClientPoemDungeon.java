package com.stars.modules.poemdungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.poemdungeon.PoemDungeonPacketSet;
import com.stars.modules.poemdungeon.teammember.RobotTeamMember;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.List;


/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ClientPoemDungeon extends PlayerPacket {
	private BaseTeamMember selfMember;
	private List<RobotTeamMember> teamMembers;// 队伍成员
	
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return PoemDungeonPacketSet.Client_PoemDungeon;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	byte size1 = (byte) (selfMember == null ? 0 : 1);   	
    	byte size2 = (byte) (teamMembers == null ? 0 : teamMembers.size());
    	byte size = (byte)(size1 + size2);
        buff.writeByte(size);
        if (selfMember != null) {
			selfMember.writeToBuffer(buff);
		}
        if (teamMembers != null) {
        	for (RobotTeamMember teamMember : teamMembers) {
                teamMember.writeToBuffer(buff);
            }
		}      
    }
    
    public void setSelfMember(BaseTeamMember selfMember){
    	this.selfMember = selfMember;
    }
    
    public void setTeamMembers(List<RobotTeamMember> teamMembers){
    	this.teamMembers = teamMembers;
    }
}