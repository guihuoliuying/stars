package com.stars.modules.sevendaygoal.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.sevendaygoal.SevenDayGoalManager;
import com.stars.modules.sevendaygoal.SevenDayGoalPacketSet;
import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientGoalData extends PlayerPacket {
	public static final byte Flag_Day_Rewards_Info = 0;
	public static final byte Flag_Update_Left_Get_Count = 1;
	public static final byte Flag_Update_Finish_Count = 2;
	public static final byte Flag_Update_OpenServer_Day = 3;
	
	private byte flag;
	
	//flag = 0
	private int days;
	//<奖励id，是否已领取>
    private Map<Integer, Byte> rewardMap = null;
    //<奖励id，剩余领取次数>
    private Map<Integer, Integer> leftCountMap = null;
    //<目标类型id，已完成次数>
    private Map<Integer, Integer> finishCountMap = null;
    
    //flag = 1
    private int updateGoalId;
    private int updateCount;
    
    //flag = 2
    //private int updateType;
    //private int updateFinishCount;
    
    //flag = 3
    //当前是开服第几天
    private int openServerDay;
    private int maxDay;
    
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SevenDayGoalPacketSet.C_GOAL_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(flag);
    	switch (flag) {
		case Flag_Day_Rewards_Info:
			writeRewardInfo(buff);
			break;
		case Flag_Update_Left_Get_Count:
			writeUpdateLeftCount(buff);
			break;
		case Flag_Update_Finish_Count:
			writeFinishCount(buff);
			break;
		case Flag_Update_OpenServer_Day:
			writeOpenServerDay(buff);
			break;
		default:
			break;
		}
    }
    
    private void writeRewardInfo(NewByteBuffer buff){
    	buff.writeInt(days);
    	
    	short size = (short) (rewardMap == null ? 0 : rewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Byte>> entrySet = rewardMap.entrySet();
            for (Map.Entry<Integer , Byte> entry : entrySet) {
            	int goalId = entry.getKey();
            	SevenDayGoalVo vo = SevenDayGoalManager.getSevenDayGoalVo(goalId);
            	if (vo == null) {
					continue;
				}
            	byte isGot = entry.getValue();
            	int leftGetCount = 0;
            	if (leftCountMap.containsKey(goalId)) {
					leftGetCount = leftCountMap.get(goalId);
				}
				
                //目标奖励id
            	buff.writeInt(goalId);
            	//描述
            	buff.writeString(vo.getDesc());
            	//达成类型
            	buff.writeInt(vo.getGoalType());
            	//达成次数
            	buff.writeInt(vo.getGoalNum());
            	//奖励
//            	Map<Integer, Integer> rewardMap = vo.getRewardMap();
//            	short size2 = (short) (rewardMap == null ? 0 : rewardMap.size());
//            	buff.writeShort(size2);
//            	Set<Map.Entry<Integer , Integer>> entrySet2 = rewardMap.entrySet();
//                for (Map.Entry<Integer , Integer> entry2 : entrySet2) {
//                	int itemId = entry2.getKey();
//                	int count = entry2.getValue();
//                	buff.writeInt(itemId);
//                	buff.writeInt(count);
//                }
            	
            	buff.writeInt(vo.getGroupId());            	
                //是否已领取
                buff.writeByte(isGot);
                //剩余领取次数
                buff.writeInt(leftGetCount);
			}
        }  
        
        short size2 = (short) (finishCountMap == null ? 0 : finishCountMap.size());
        buff.writeShort(size2);
        if (size2 != 0) {
            Set<Map.Entry<Integer , Integer>> entrySet = finishCountMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
            	int goalType = entry.getKey();
            	int finishCount = entry.getValue();				
                //目标类型
            	buff.writeInt(goalType);
            	//完成次数
            	buff.writeInt(finishCount);
			}
        }   
    }
     
    private void writeUpdateLeftCount(NewByteBuffer buff){
    	//更新的目标id
    	buff.writeInt(updateGoalId);
    	//更新的目标剩余可领次数
    	buff.writeInt(updateCount);
    }
    
    private void writeFinishCount(NewByteBuffer buff){
    	//更新的类型
    	//buff.writeInt(updateType);
    	//更新的完成次数
    	//buff.writeInt(updateFinishCount);
    	
        short size2 = (short) (finishCountMap == null ? 0 : finishCountMap.size());
        buff.writeShort(size2);
        if (size2 != 0) {
            Set<Map.Entry<Integer , Integer>> entrySet = finishCountMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
            	int goalType = entry.getKey();
            	int finishCount = entry.getValue();				
                //目标类型
            	buff.writeInt(goalType);
            	//完成次数
            	buff.writeInt(finishCount);
			}
        }
    }
    
    private void writeOpenServerDay(NewByteBuffer buff){
    	//更新的当前天数
    	buff.writeInt(openServerDay);
    	buff.writeInt(maxDay);
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setDays(int value){
    	this.days = value;
    }
    
    public void setRewardMap(Map<Integer, Byte> value){
    	this.rewardMap = value;
    }
    
    public void setLeftCountMap(Map<Integer, Integer> value){
    	this.leftCountMap = value;
    }
    
    public void setFinishCountMap(Map<Integer, Integer> value){
    	this.finishCountMap = value;
    }
    
    public void setUpdateGoalId(int value){
    	this.updateGoalId = value;
    }
    
    public void setUpdateCount(int value){
    	this.updateCount = value;
    }
    
//    public void setUpdateType(int value){
//    	this.updateType = value;
//    }
//    
//    public void setUpdateFinishCount(int value){
//    	this.updateFinishCount = value;
//    }
    
    public void setOpenServerDay(int value){
    	this.openServerDay = value;
    }
    
    public void setMaxDay(int value){
    	this.maxDay = value;
    }
}