package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.gem.packet.vo.ClientGemOprVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应客户端装备提升的影响值;
 * Created by panzhenfeng on 2016/6/30.
 */
public class ClientGemTishenOpr extends PlayerPacket {
    private byte resultType = 0; //0成功, 1失败;

    private List<ClientGemOprVo> waitToSendList = new ArrayList<>();

    public ClientGemTishenOpr() {
        resultType = 0;
    }

    public ClientGemTishenOpr(boolean isSuccess) {
        resultType = isSuccess ? (byte) 0 : (byte) 1;
    }

    public void addData(byte equipmentType, String resultContent, byte tishenOprType) {
        ClientGemOprVo clientGemOprVo = new ClientGemOprVo();
        clientGemOprVo.equipmentType = equipmentType;
        clientGemOprVo.resultContent = resultContent;
        clientGemOprVo.tishenOprType = tishenOprType;
        waitToSendList.add(clientGemOprVo);
    }


    public void addData(byte equipmentType){
        ClientGemOprVo clientGemOprVo = new ClientGemOprVo();
        clientGemOprVo.equipmentType = equipmentType;
        waitToSendList.add(clientGemOprVo);
    }


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GemPacketSet.C_GEM_TISHEN_OPR;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resultType);
        buff.writeInt(waitToSendList.size());
        ClientGemOprVo clientGemOprVo = null;
        for (int i = 0, len = waitToSendList.size(); i < len; i++) {
            clientGemOprVo = waitToSendList.get(i);
            buff.writeByte(clientGemOprVo.equipmentType);
            if(resultType == 0){
                buff.writeString(clientGemOprVo.resultContent);
                buff.writeByte(clientGemOprVo.tishenOprType);
            }
        }
    }

}