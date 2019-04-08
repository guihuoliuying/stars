package com.stars.modules.popUp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.popUp.PopUpPacketSet;
import com.stars.modules.popUp.prodata.PopUpInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class ClientPopUp extends PlayerPacket {

    public static final byte RESP_POPUP = 0x00; // 弹窗序列

    public byte subtype;
    private List<PopUpInfo> popUpList;

    public ClientPopUp() {
    }

    public ClientPopUp(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_POPUP:
                writePopUpToBuff(buff);
                break;
        }
    }

    private void writePopUpToBuff(NewByteBuffer buff){
        if(StringUtil.isEmpty(popUpList)){
            buff.writeByte((byte)0);
        }else{
            buff.writeByte((byte)popUpList.size());
            for(PopUpInfo popUpInfo:popUpList){
                popUpInfo.writeToBuff(buff);
            }
        }
    }

    @Override
    public short getType() {
        return PopUpPacketSet.C_POPUP;
    }

    public void setPopUpList(List<PopUpInfo> popUpList) {
        this.popUpList = popUpList;
    }

    @Override
    public void execPacket(Player player) {

    }
}
