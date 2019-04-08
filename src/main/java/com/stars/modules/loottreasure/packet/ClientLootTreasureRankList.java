package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.scene.SceneManager;
import com.stars.multiserver.LootTreasure.LTDamageRankVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * 告诉客户端当前的伤害排名情况;
 * Created by panzhenfeng on 2016/10/12.
 */
public class ClientLootTreasureRankList  extends PlayerPacket {
    private List<LTDamageRankVo> rankFirstList = null;
    private LTDamageRankVo selfRankVo = null;
    private boolean needSendSelfData = true;
    private byte curStageType = SceneManager.SCENETYPE_LOOTTREASURE_PVE;

    public ClientLootTreasureRankList(){}

    public ClientLootTreasureRankList(byte curStageType, List<LTDamageRankVo> rankFirstList) {
        this.curStageType = curStageType;
        this.rankFirstList = rankFirstList;
    }

    public void setMySelfRankVo(LTDamageRankVo selfRankVo){
        this.selfRankVo = selfRankVo;
        //判断列表里是不是已经有了玩家自身的排名信息,有了的话就不下发了;
        if(this.rankFirstList.contains(this.selfRankVo)){
            needSendSelfData = false;
        }else {
            needSendSelfData = true;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_RANKLIST;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.curStageType);
        int len = rankFirstList==null? 0:rankFirstList.size();
        buff.writeByte((byte)len);
        for (int i = 0; i<len; i++){
            rankFirstList.get(i).writeBuff(buff);
        }
        boolean isValidToSendSelf = needSendSelfData && selfRankVo != null;
        buff.writeByte(isValidToSendSelf ? (byte)1: (byte)0);
        if(isValidToSendSelf){
            selfRankVo.writeBuff(buff);
        }
    }
}