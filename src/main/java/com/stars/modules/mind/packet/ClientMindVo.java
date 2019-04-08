package com.stars.modules.mind.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.mind.MindPacketSet;
import com.stars.modules.mind.prodata.MindVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;


/**
 * 响应客户端请求心法表数据
 * Created by gaopeidian on 2016/9/24.
 */
public class ClientMindVo extends PlayerPacket {
    private Map<Integer, MindVo> mindVoMap;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MindPacketSet.C_MIND_VO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	short size = (short) (mindVoMap == null ? 0 : mindVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (MindVo mindVo : mindVoMap.values()) {
            	int mindId = mindVo.getMindId();
            	int order = mindVo.getOrder();
            	String premind = mindVo.getPreMind();
            	String icon = mindVo.getIcon();
            	String name = mindVo.getName();
            	String active = mindVo.getActive();
            	String activeCost = mindVo.getActivecost();
            	
            	buff.writeInt(mindId);
            	buff.writeInt(order);
            	buff.writeString(premind);
            	buff.writeString(icon);
            	buff.writeString(name);
            	buff.writeString(active);
            	buff.writeString(activeCost);
            }
        }
    }
    
    public void setMindVoMap(Map<Integer, MindVo> mindVoMap){
    	this.mindVoMap = mindVoMap;
    }
}