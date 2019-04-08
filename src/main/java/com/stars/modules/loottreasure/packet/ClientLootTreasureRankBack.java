package com.stars.modules.loottreasure.packet;

import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.multiserver.LootTreasure.LTDamageRankVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 夺宝服返回排行榜数据;
 * Created by panzhenfeng on 2016/10/21.
 */
public class ClientLootTreasureRankBack  extends Packet {
    public Map<String, List<LTDamageRankVo>> pvpDamageRankVoMap = null;

    public ClientLootTreasureRankBack() {

    }

    public void addListRankVoList(String id, List<LTDamageRankVo> list){
        if(pvpDamageRankVoMap == null){
            pvpDamageRankVoMap = new HashMap<>();
        }
        pvpDamageRankVoMap.put(id, list);
    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_RANK_BACK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int size = pvpDamageRankVoMap == null?0: pvpDamageRankVoMap.size();
        buff.writeInt(size);
        List<LTDamageRankVo> tmpList = null;
        for (Map.Entry<String, List<LTDamageRankVo>> kvp : pvpDamageRankVoMap.entrySet()){
            buff.writeString(kvp.getKey());
            buff.writeInt(kvp.getValue().size());
            for(int i = 0, len = kvp.getValue().size(); i<len; i++){
                kvp.getValue().get(i).writeBuff(buff);
            }
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        pvpDamageRankVoMap = new HashMap<>();
        String tmpId;
        int size = buff.readInt();
        int len = 0;
        List<LTDamageRankVo> damageRankVoList = null;
        LTDamageRankVo ltDamageRankVo = null;
        for(int i = 0; i<size; i++){
            tmpId = buff.readString();
            damageRankVoList = new ArrayList<>();
            len = buff.readInt();
            for(int k = 0; k<len; k++){
                ltDamageRankVo = new LTDamageRankVo();
                ltDamageRankVo.readBuff(buff);
                damageRankVoList.add(ltDamageRankVo);
            }
            pvpDamageRankVoMap.put(tmpId, damageRankVoList);
        }
    }

    @Override
    public void execPacket() {
        //TODO 调用ClientLootTreasureRankList发送到客户端，走统一流程;
    }

}