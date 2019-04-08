package com.stars.modules.bravepractise.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.bravepractise.BravePractisePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;


/**
 * 响应客户端请求勇者试炼页面数据
 * Created by gaopeidian on 2016/11/17.
 */
public class ClientBravePageInfo extends PlayerPacket {
    private Map<Integer, Integer> rewardMap;
    private int leftCount;
    //private int totalCount;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BravePractisePacketSet.C_BRAVE_PAGE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	//奖励
    	short size = (short) (rewardMap == null ? 0 : rewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Integer>> entrySet = rewardMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
				int itemId = entry.getKey();
				int itemCount = entry.getValue();
				buff.writeInt(itemId);
				buff.writeInt(itemCount);
			}
        }
        
        //剩余数量
        buff.writeInt(leftCount);
        
        //总数量
        //buff.writeInt(totalCount);
    }
    
    public void setRewardMap(Map<Integer, Integer> rewardMap){
    	this.rewardMap = rewardMap;
    }
    
    public void setLeftCount(int value){
    	this.leftCount = value;
    }
    
//    public void setTotalCount(int value){
//    	this.totalCount = value;
//    }
}