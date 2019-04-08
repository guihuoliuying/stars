package com.stars.modules.mind.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.mind.MindConstant;
import com.stars.modules.mind.MindPacketSet;
import com.stars.modules.mind.userdata.RoleMind;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;


/**
 * 响应客户端请求心法数据
 * Created by gaopeidian on 2016/9/24.
 */
public class ClientMindInfo extends PlayerPacket {
    private Map<Integer, RoleMind> roleMindMap;
    private Map<Integer, Byte> stateMap;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MindPacketSet.C_MIND_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	short size = (short) (roleMindMap == null ? 0 : roleMindMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (RoleMind roleMind : roleMindMap.values()) {
            	int mindId = roleMind.getMindId();
            	int mindLevel = roleMind.getMindLevel();
            	
            	byte state = MindConstant.MIND_STATE_CAN_NOT_ACTIVE;
            	if (stateMap.containsKey(mindId)) {
            		state = stateMap.get(mindId);//心法状态，0：已激活，1：未激活且可激活，2：未激活且不可激活，详见MindConstant的描述
				}
            	           	
            	buff.writeInt(mindId);
            	buff.writeInt(mindLevel);
            	buff.writeByte(state);
            }
        }
    }
    
    public void setRoleMindMap(Map<Integer, RoleMind> roleMindMap){
    	this.roleMindMap = roleMindMap;
    }
    
    public void setStateMap(Map<Integer, Byte> stateMap){
    	this.stateMap = stateMap;
    }
}