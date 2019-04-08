package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientDaily5v5Revive extends PlayerPacket {
	
	public static byte TYPE_OF_COUNT = 1;//复活次数
	public static byte TYPE_OF_TIME = 2;//复活时间
	
	private byte type;
	private int reviveCount = 0;
	private int reviveTime = 0;
	private String buffName;
	private int level;
	private int reduceTime;//buff效果减少的时间

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5Revive;
	}
	
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(type);
    	if (type == TYPE_OF_COUNT) {	//type==1
    		buff.writeInt(FamilyActWarManager.totalPayRevive - reviveCount);//剩余复活次数
	//    	buff.writeInt(FamilyActWarManager.totalPayRevive);//最多复活次数
	//    	buff.writeInt(FamilyActWarManager.revivePay);//复活消耗
    	} else if (type == TYPE_OF_TIME) {//type==2
			buff.writeInt(reviveTime);	//复活剩余时间
			buff.writeString(buffName);
			buff.writeInt(level);
			buff.writeInt(reduceTime);
		}
    }

	public int getReviveTime() {
		return reviveTime;
	}

	public void setReviveTime(int reviveTime) {
		this.reviveTime = reviveTime;
	}

	public int getReviveCount() {
		return reviveCount;
	}

	public void setReviveCount(int reviveCount) {
		this.reviveCount = reviveCount;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public static byte getTYPE_OF_COUNT() {
		return TYPE_OF_COUNT;
	}

	public static void setTYPE_OF_COUNT(byte tYPE_OF_COUNT) {
		TYPE_OF_COUNT = tYPE_OF_COUNT;
	}

	public static byte getTYPE_OF_TIME() {
		return TYPE_OF_TIME;
	}

	public static void setTYPE_OF_TIME(byte tYPE_OF_TIME) {
		TYPE_OF_TIME = tYPE_OF_TIME;
	}

	public String getBuffName() {
		return buffName;
	}

	public void setBuffName(String buffName) {
		this.buffName = buffName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getReduceTime() {
		return reduceTime;
	}

	public void setReduceTime(int reduceTime) {
		this.reduceTime = reduceTime;
	}

}
