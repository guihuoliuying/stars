package com.stars.modules.gameboard.packet;

import com.stars.modules.gameboard.GameboardPacketSet;
import com.stars.modules.gameboard.prodata.GameboardVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by chenkeyu on 2017/1/5 19:58
 */
public class ClientGameboard extends Packet {
    private List<GameboardVo> GameboardVos;
    @Override
    public short getType() {
        return GameboardPacketSet.C_GAMEBOARD;
    }



    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) GameboardVos.size());
        for (GameboardVo vo : GameboardVos){
            vo.writeToBuffer(buff);
        }

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public List<GameboardVo> getGameboardVos() {
        return GameboardVos;
    }

    public void setGameboardVos(List<GameboardVo> gameboardVos) {
        GameboardVos = gameboardVos;
    }
}
