package com.stars.modules.sendvigour.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.sendvigour.SendVigourPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/3/30.
 */
public class ClientSendVigourData extends PlayerPacket {
	private byte getStatus;//是否可领，0为不可领，1为可领
	private String cronexpr;//下次领取时间
	private byte isTomorrow;//是否是明天的，0为不是，1为是
	
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SendVigourPacketSet.C_SEND_VIGOUR_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(getStatus);
    	buff.writeString(cronexpr);
    	buff.writeByte(isTomorrow);
    }
    
    public void setGetStatus(byte getStatus){
    	this.getStatus = getStatus;
    }
    
    public void setCronexpr(String cronexpr){
    	this.cronexpr = cronexpr;
    }
    
    public void setIsTomorrow(byte isTomorrow){
    	this.isTomorrow = isTomorrow;
    }
}