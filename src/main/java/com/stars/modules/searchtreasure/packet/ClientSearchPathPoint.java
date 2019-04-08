package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.searchtreasure.SearchTreasureConstant;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;

/**
 * 响应客户端请求获取探索点的content;
 * Created by panzhenfeng on 2016/8/24.
 */
public class ClientSearchPathPoint  extends PlayerPacket {
    private Map<Integer, Integer> awardMap;
    private byte oprType;

    public ClientSearchPathPoint() {

    }
    public ClientSearchPathPoint(byte oprType) {
        this.oprType = oprType;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap){
        this.awardMap = awardMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.C_SEARCHTREASURE_PATH_POINT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.oprType);
        switch (this.oprType){
            case SearchTreasureConstant.CONTENTTYPE_REWARD:
                buff.writeInt(this.awardMap.size());
                Set<Integer> keyset = this.awardMap.keySet();
                for(Integer key : keyset){
                    buff.writeInt(key);
                    buff.writeInt(this.awardMap.get(key));
                }
                break;
        }
    }

}
