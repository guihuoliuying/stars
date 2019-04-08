package com.stars.modules.callboss.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.callboss.cache.CallRecordCache;
import com.stars.services.callboss.cache.RoleDamageCache;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ClientCallBossRankList extends PlayerPacket {
    Map<Integer, CallRecordCache> map;
    long toRoleId;

    public ClientCallBossRankList() {
    }

    public ClientCallBossRankList(Map<Integer, CallRecordCache> map , long toRoleId) {
        this.map = map;
        this.toRoleId = toRoleId;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return CallBossPacketSet.C_CALLBOSS_RANKLIST;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
//        short size = (short) (map == null ? 0 : map.size());
//        buff.writeShort(size);
//        if (size <= 0)
//            return;
//        for (CallRecordCache recordCache : map.values()) {
//            recordCache.writeToBuff(buff);
//        }
    	
    	short size = (short) (map == null ? 0 : map.size());
        buff.writeShort(size);
        if (size <= 0)
            return;
        for (CallRecordCache recordCache : map.values()) {
        	int uniqueId = recordCache.getUniqueId();
        	int bossId = recordCache.getBossId();
        	long callRoleId = recordCache.getCallRoleId();
        	String callRoleName = recordCache.getCallRoleName();
        	long callTime = recordCache.getCallTime();
        	
        	byte isIn = (byte)0;
        	RoleDamageCache myDamageCache = recordCache.getRoleDamageRank(this.toRoleId);
        	if (myDamageCache != null) {
				isIn = (byte)1;
			}
        	
        	buff.writeInt(uniqueId);
            buff.writeInt(bossId);
            buff.writeString(String.valueOf(callRoleId));
            buff.writeString(callRoleName);
            buff.writeLong(callTime);
            buff.writeByte(isIn);
        }
    }
    
    public void setToRoleId(long value){
    	this.toRoleId = value;
    }
}
