package com.stars.modules.familyactivities.war.packet.fight;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

public class ClientFamilyWarFightStageResult extends PlayerPacket {

	private boolean isWin;
    private int moraleDelta;
    private Map<Integer, Integer> toolMap;
	private byte isElite;//0:普通成员，1：精英成员
	private long points;
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_STAGE_RESULT;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(isElite);//0:普通成员，1：精英成员
		buff.writeString(Long.toString(points)); // 个人积分
		buff.writeByte(isWin ? TRUE : FALSE); // 是否胜利，1 - 胜利，0 - 失败
        buff.writeInt(moraleDelta); // 增加的士气量
        buff.writeInt(toolMap == null ? 0 : toolMap.size()); // 道具列表大小
        if (toolMap != null) {
        	for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
        		buff.writeInt(entry.getKey()); // itemId
        		buff.writeInt(entry.getValue()); // item count
        	}
        }
	}

	public void setIsElite(byte isElite) {
		this.isElite = isElite;
	}

	public void setPoints(long points) {
		this.points = points;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	public int getMoraleDelta() {
		return moraleDelta;
	}

	public void setMoraleDelta(int moraleDelta) {
		this.moraleDelta = moraleDelta;
	}

	public Map<Integer, Integer> getToolMap() {
		return toolMap;
	}

	public void setToolMap(Map<Integer, Integer> toolMap) {
		this.toolMap = toolMap;
	}

}
