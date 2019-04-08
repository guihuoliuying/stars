package com.stars.modules.sendvigour.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.sendvigour.SendVigourModule;
import com.stars.modules.sendvigour.SendVigourPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/3/30.
 */
public class ServerSendVigourData  extends PlayerPacket {
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		
	}
	
    @Override
    public void execPacket(Player player) {
    	SendVigourModule sendVigourModule = (SendVigourModule)module(MConst.SendVigour);
    	sendVigourModule.sendData();
    }

    @Override
    public short getType() {
        return SendVigourPacketSet.S_SEND_VIGOUR_DATA;
    }  
}
