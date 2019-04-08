package com.stars.modules.retrievereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.retrievereward.RetrieveRewardManager;
import com.stars.modules.retrievereward.RetrieveRewardPacketSet;
import com.stars.modules.retrievereward.prodata.RetrieveRewardVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientRetrieveReward extends PlayerPacket {
	public static final byte Flag_Reward_Info = 0;
	public static final byte Flag_Get_Reward =1;
	
	private byte flag;
	
	//<奖励id，可找回次数> flag = 0 用
    private Map<Integer, Integer> retrieveRewardMap = null;
    
    //<奖励id，可找回次数> flag = 1 用
    private Map<Integer, Integer> updateRewardMap = null;

    //flag = 0 、 flag = 1 共用
    private Map<Integer, Byte> retrieveRewardStatusMap = null;
    
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RetrieveRewardPacketSet.C_RETRIEVE_REWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(flag);
    	switch (flag) {
		case Flag_Reward_Info:
			writeRewardInfo(buff);
			break;
		case Flag_Get_Reward:
			writeGetReward(buff);
			break;
		default:
			break;
		}
    }
    
    private void writeRewardInfo(NewByteBuffer buff){
    	short size = (short) (retrieveRewardMap == null ? 0 : retrieveRewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Integer>> entrySet = retrieveRewardMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
            	int rewardId = entry.getKey();
            	RetrieveRewardVo retrieveRewardVo = RetrieveRewardManager.getRetrieveRewardVo(rewardId);
            	if (retrieveRewardVo == null) {
					continue;
				}
            	byte isGot = 0;
            	if (retrieveRewardStatusMap.containsKey(rewardId)) {
					isGot = retrieveRewardStatusMap.get(rewardId);
				}
            	int retrieveCount = entry.getValue();
            	
                //奖励id
            	buff.writeInt(rewardId);
            	//日常id
            	buff.writeShort(retrieveRewardVo.getDailyid());
            	//领取状态 0：不可领 1：可领
            	buff.writeByte(isGot);
            	//找回次数
            	buff.writeInt(retrieveCount);
            	//类型
            	buff.writeByte(retrieveRewardVo.getType());
            	//奖励
//            	Map<Integer, Integer> rewardMap = retrieveRewardVo.getRewardMap();
//            	short size2 = (short) (rewardMap == null ? 0 : rewardMap.size());
//            	buff.writeShort(size2);
//            	Set<Map.Entry<Integer , Integer>> entrySet2 = rewardMap.entrySet();
//                for (Map.Entry<Integer , Integer> entry2 : entrySet2) {
//                	int itemId = entry2.getKey();
//                	int count = entry2.getValue();
//                	buff.writeInt(itemId);
//                	buff.writeInt(count);
//                }
            	buff.writeInt(retrieveRewardVo.getGroupId());
            	
                //花费
            	Map<Integer, Integer> costMap = retrieveRewardVo.getCostMap();
            	short size3 = (short) (costMap == null ? 0 : costMap.size());
            	buff.writeShort(size3);
            	Set<Map.Entry<Integer , Integer>> entrySet3 = costMap.entrySet();
                for (Map.Entry<Integer , Integer> entry3 : entrySet3) {
                	int itemId = entry3.getKey();
                	int count = entry3.getValue();
                	buff.writeInt(itemId);
                	buff.writeInt(count);
                }	
			}
        }   
    }
    
    private void writeGetReward(NewByteBuffer buff){
    	short size = (short) (updateRewardMap == null ? 0 : updateRewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
        	Set<Map.Entry<Integer , Integer>> entrySet = updateRewardMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
            	int getRewardId = entry.getKey();
            	RetrieveRewardVo retrieveRewardVo = RetrieveRewardManager.getRetrieveRewardVo(getRewardId);
            	byte getRewardType = 0;
            	if (retrieveRewardVo != null) {
            		getRewardType = retrieveRewardVo.getType();
				}else{
				}
            	byte isGot = 0;
            	if (retrieveRewardStatusMap.containsKey(getRewardId)) {
					isGot = retrieveRewardStatusMap.get(getRewardId);
				}
            	int leftRetieveCount = entry.getValue();
            	
            	buff.writeInt(getRewardId);
            	buff.writeByte(getRewardType);
            	buff.writeByte(isGot);
            	buff.writeInt(leftRetieveCount);
            }
        }
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setRetrieveRewardMap(Map<Integer, Integer> value){
    	this.retrieveRewardMap = value;
    }
    
    public void setRetrieveRewardStatusMap(Map<Integer, Byte> value){
    	this.retrieveRewardStatusMap = value;
    }
    
    public void setUpdateRewardMap(Map<Integer, Integer> value){
    	this.updateRewardMap = value;
    }
   
}