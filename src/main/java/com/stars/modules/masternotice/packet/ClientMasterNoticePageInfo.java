package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.modules.masternotice.recordmap.MasterNoticeData;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientMasterNoticePageInfo extends PlayerPacket {
    private int leftCount;
    private int totalCount;
    private int refreshTime;
    private int leftRefreshCount;
    private int leftCostRefreshCount;
    private Map<Integer, MasterNoticeData> noticesMap;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MasterNoticePacketSet.C_MASTER_PAGE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        //剩余数量
        buff.writeInt(leftCount);
        
        //总数量
        //buff.writeInt(totalCount);
        
        //倒计时剩余时间，单位：秒
        buff.writeInt(refreshTime);
        
        //剩余免费刷新次数
        buff.writeInt(leftRefreshCount);
    	
    	//悬赏任务
    	short size = (short) (noticesMap == null ? 0 : noticesMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (MasterNoticeData data : noticesMap.values()) {
				int noticeId = data.getNoticeId();
				byte status = data.getStatus();
				buff.writeInt(noticeId);
				buff.writeByte(status);//任务状态，0：未接受，1：接受但未完成，2：已完成
			}
        }
        
        //剩余付费刷新次数
        buff.writeInt(leftCostRefreshCount);
    }
    
    public void setLeftCount(int value){
    	this.leftCount = value;
    }
    
    public void setTotalCount(int value){
    	this.totalCount = value;
    }
    
    public void setRefreshTime(int value){
    	this.refreshTime = value;
    }
    
    public void setLeftRefreshCount(int value){
    	this.leftRefreshCount = value;
    }
    
    public void setLeftCostRefreshCount(int value){
    	this.leftCostRefreshCount = value;
    }
    
    public void setNoticesMap(Map<Integer, MasterNoticeData> value){
    	this.noticesMap = value;
    }
}