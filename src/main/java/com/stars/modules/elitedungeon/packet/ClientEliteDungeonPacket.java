package com.stars.modules.elitedungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.elitedungeon.EliteDungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/11.
 */
public class ClientEliteDungeonPacket extends PlayerPacket {
	public static final byte BACK_TO_CITY = 1;// 回城消息
    public static final byte CHANGE_TARGET_TIPS = 2;// 切换目标有不满足条件队员的提示
    public static final byte ENTER_FIGHT_TIPS = 3;// 进入副本有不满足条件队员的提示
	
	private byte sendType;// 下发类型
	
	private Map<Long, String> memberNameMap;
	private Map<Long, Byte> tipsTypeMap;
	private int targetEliteId;
	public static final byte TIPS_NOT_ACTIVE = 1;// 未激活副本
    public static final byte TIPS_NOT_ENOUGH_VIGOUR = 2;// 体力不足
    public static final byte TIPS_NO_TIMES = 3;// 次数不够

    /* 参数 */
    private Map<Integer, Integer> itemMap = new HashMap<Integer, Integer>();

    public ClientEliteDungeonPacket() {
    	
    }

    public ClientEliteDungeonPacket(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return EliteDungeonPacketSet.Client_EliteDungeon;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case BACK_TO_CITY:{
            	short size = (short) (itemMap == null ? 0 : itemMap.size());
                buff.writeShort(size);
                if (size != 0) {
                    for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
            }
            case CHANGE_TARGET_TIPS:
            case ENTER_FIGHT_TIPS:{
            	short size = (short) (memberNameMap == null ? 0 : memberNameMap.size());
                buff.writeShort(size);
                if (size != 0) {
                    for (Map.Entry<Long, String> entry : memberNameMap.entrySet()) {
                        long roleId = entry.getKey();
                    	buff.writeString(entry.getValue());
                        byte tipsType = TIPS_NOT_ACTIVE;
                        if (tipsTypeMap.containsKey(roleId)) {
							tipsType = tipsTypeMap.get(roleId);
						}
                        buff.writeByte(tipsType);
                    }
                }
                
                buff.writeInt(targetEliteId);
                
                break;
            }    
        }
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }
    
    public void setMemberNameMap(Map<Long, String> memberNameMap) {
        this.memberNameMap = memberNameMap;
    }
    
    public void setTipsTypeMap(Map<Long, Byte> tipsTypeMap) {
        this.tipsTypeMap = tipsTypeMap;
    }
    
    public void setTargetEliteId(int targetEliteId){
    	this.targetEliteId = targetEliteId;
    }
}
