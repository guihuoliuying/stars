package com.stars.modules.newsignin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newsignin.NewSigninPacketSet;
import com.stars.modules.newsignin.prodata.SigninVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by chenkeyu on 2017/2/8 9:44
 */
public class ClientSigninVo extends PlayerPacket {

    public ClientSigninVo() {}

    private List<SigninVo> singleSignList;
    private List<SigninVo> accumulateAwardlist;
    private List<SigninVo> specialAwardList;


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewSigninPacketSet.C_SigninVo;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeToBuffer(buff, singleSignList);
        writeToBuffer(buff, accumulateAwardlist);
        writeToBuffer(buff, specialAwardList);
    }

    private void writeToBuffer(NewByteBuffer buff, List<SigninVo> list) {
        buff.writeByte((byte) list.size());
        for (SigninVo signinVo : list) {
            signinVo.writeToBuffer(buff);
        }
    }

    public void setSingleSignList(List<SigninVo> singleSignList) {
        this.singleSignList = singleSignList;
    }

    public void setAccumulateAwardlist(List<SigninVo> accumulateAwardlist) {
        this.accumulateAwardlist = accumulateAwardlist;
    }

    public void setSpecialAwardList(List<SigninVo> specialAwardList) {
        this.specialAwardList = specialAwardList;
    }
}
