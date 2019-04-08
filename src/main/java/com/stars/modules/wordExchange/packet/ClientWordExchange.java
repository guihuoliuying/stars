package com.stars.modules.wordExchange.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.wordExchange.WordExchangePacketSet;
import com.stars.modules.wordExchange.vo.ExchangeVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class ClientWordExchange extends PlayerPacket {

    public static final byte RESP_VIEW = 0x01; // 查看集字活动界面

    private byte subtype;
    private List<ExchangeVo> voList;
    private String ruledesc;

    public ClientWordExchange() {
    }

    public ClientWordExchange(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_VIEW:
                writeViewToBuff(buff);
                break;
        }
    }

    private void writeViewToBuff(NewByteBuffer buff){
        buff.writeString(ruledesc);
        if(StringUtil.isEmpty(voList)){
            buff.writeInt(0);
        }else{
            buff.writeInt(voList.size());
            for(ExchangeVo vo:voList){
                vo.writeToBuff(buff);
            }
        }
    }

    @Override
    public short getType() {
        return WordExchangePacketSet.C_WORDEXCHANGE;
    }

    public List<ExchangeVo> getVoList() {
        return voList;
    }

    public void setVoList(List<ExchangeVo> voList) {
        this.voList = voList;
    }

    public String getRuledesc() {
        return ruledesc;
    }

    public void setRuledesc(String ruledesc) {
        this.ruledesc = ruledesc;
    }

    @Override
    public void execPacket(Player player) {

    }
}
