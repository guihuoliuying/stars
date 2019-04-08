package com.stars.modules.newserverrank.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newserverrank.NewServerRankPacketSet;
import com.stars.modules.newserverrank.prodata.NewServerRankVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientNewServerRank extends PlayerPacket {
	public static final byte Flag_Get_Reward_Info = 0;
	public static final byte Flag_Get_Rank_Info = 1;
	
	private byte flag;
	
	//flag = 0
	private int rankType;
	private List<NewServerRankVo> rankRewardVoList;
	private long startTimeStamp;
	private long endTimeStamp;
	
	//flag = 1
    private Map<Long, String> rankMap;
    private int myRank;
	
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewServerRankPacketSet.C_NEW_SERVER_RANK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(flag);
    	switch (flag) {
		case Flag_Get_Reward_Info:
			writeRewardInfo(buff);
			break;
		case Flag_Get_Rank_Info:
			writeRankInfo(buff);
			break;
		default:
			break;
		}
    }
    
    private void writeRewardInfo(NewByteBuffer buff){
    	//排名类型
    	buff.writeInt(rankType);
    	
    	short size = (short) (rankRewardVoList == null ? 0 : rankRewardVoList.size());
        buff.writeShort(size);
        if (size != 0) {
        	for (NewServerRankVo vo : rankRewardVoList) {
        		//奖励起始名次
				buff.writeInt(vo.getRankStart());
				//奖励结束名次
				buff.writeInt(vo.getRankEnd());
			 	// 显示奖励
				buff.writeString(vo.getShowReward());
			}
        } 
        
        //活动开始时间
        buff.writeString(Long.toString(startTimeStamp));
    	//活动结束时间
        buff.writeString(Long.toString(endTimeStamp));
    }
    
    private void writeRankInfo(NewByteBuffer buff){
        short size = (short)(rankMap == null ? 0 : rankMap.size());
        buff.writeShort(size);
        if (size != 0) {
			Set<Map.Entry<Long, String>> entrySet = rankMap.entrySet();
			for (Map.Entry<Long, String> entry : entrySet) {
				long roleId = entry.getKey();
				String name = entry.getValue();
				buff.writeLong(roleId);
				buff.writeString(name);
			}
		}
    	buff.writeInt(myRank);
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setRankType(int value){
    	this.rankType = value;
    }
    
    public void setRankRewardVoList(List<NewServerRankVo> value){
    	this.rankRewardVoList = value;
    }
    
    public void setStartTimeStamp(long value){
    	this.startTimeStamp = value;
    }
    
    public void setEndTimeStamp(long value){
    	this.endTimeStamp = value;
    }
   
    public void setrankMap(Map<Long, String> value){
    	this.rankMap = value;
    }
    
    public void setMyRank(int value){
    	this.myRank = value;
    }   
}