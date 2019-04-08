package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.modules.gamecave.userdata.RoleGameCave;
import com.stars.modules.gamecave.userdata.TinyGameData;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientGameRecordData extends PlayerPacket {
    private List<TinyGameData> finishGameDatas;
    private RoleGameCave roleGameCave;

    public ClientGameRecordData() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_GAME_RECORD_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	short size = (short)(finishGameDatas == null ? 0 : finishGameDatas.size());
    	buff.writeShort(size);
    	if (size != 0) {
			for (TinyGameData data : finishGameDatas) {
				data.writeRecordData(buff);
			}
		}
    	
    	roleGameCave.writeRecordData(buff);
    }
 
    public void setFinishGameDatas(List<TinyGameData> value){
    	this.finishGameDatas = value;
    }
    
    public void setRoleGameCave(RoleGameCave value){
    	this.roleGameCave = value;
    }
}
