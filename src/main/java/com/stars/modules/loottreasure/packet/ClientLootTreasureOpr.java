package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一些野外夺宝的杂项协议;
 * Created by panzhenfeng on 2016/10/19.
 */
public class ClientLootTreasureOpr  extends PlayerPacket {

    public final static byte KILL = 1; //击杀
    public final static byte BE_KILLED = 2;//被杀
    public final static byte GAINBOX = 3;//获得宝箱;
    public final static byte LOSEBOX = 4;//丢失宝箱;
    public final static byte ROOMBOX = 5;//房间宝箱情况; 这个应该在第一次时将房间的宝箱情况全部下发,在房间里时，结合ClientLootTreasureRankList协议下发，减少数据量;
    public final static byte MATCH_SUC = 6; // 匹配成功

    private byte oprType = -1;
    public String roleName;
    public short count;
    private Map<Long, Short> boxCountMap ;

    public ClientLootTreasureOpr(){}

    public ClientLootTreasureOpr(byte oprType) {
        this.oprType = oprType;
    }

    public void addBoxCount(long roleId, int boxCount){
        boxCountMap = boxCountMap == null?new ConcurrentHashMap<Long, Short>():boxCountMap;
        boxCountMap.put(roleId, (short)boxCount);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_OPR;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.oprType);
        switch (this.oprType){
            case KILL:
                buff.writeString(this.roleName);
                break;
            case BE_KILLED:
                buff.writeString(this.roleName);
                buff.writeShort(this.count);
                break;
            case GAINBOX:
                buff.writeShort(count);
                break;
            case LOSEBOX:
                buff.writeShort(count);
                break;
            case ROOMBOX:
                buff.writeByte((byte) boxCountMap.size());
                for(Map.Entry<Long, Short> kvp : boxCountMap.entrySet()){
                    buff.writeString(String.valueOf(kvp.getKey()));
                    buff.writeShort(kvp.getValue());
                }
                break;
        }
    }

}