package com.stars.modules.escort.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.escort.EscortPacketSet;
import com.stars.modules.escort.packet.vo.CargoPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wuyuxing on 2016/12/8.
 * 运镖队列场景相关
 */
public class ClientEscortSafe extends PlayerPacket {

    public static final byte RESP_ENTER_SENCE = 0x00;   // 进入队列场景
    public static final byte RESP_UPDATE_CARGO = 0x01;  // 刷新镖车

    private byte subtype;
    private List<CargoPo> cargoList;

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_ENTER_SENCE:
                if(StringUtil.isEmpty(cargoList)){
                    buff.writeByte((byte)0);
                }else{
                    buff.writeByte((byte)cargoList.size());
                    for(CargoPo po:cargoList){
                        po.writeToBuff(buff);
                    }
                }
                break;
            case RESP_UPDATE_CARGO:
                if(StringUtil.isEmpty(cargoList)){
                    buff.writeByte((byte)0);
                }else{
                    buff.writeByte((byte)cargoList.size());
                    for(CargoPo po:cargoList){
                        po.writeToBuff(buff);
                    }
                }
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff){
        this.subtype = buff.readByte();
        byte size;
        List<CargoPo> tmpList;
        CargoPo po;
        switch (subtype) {
            case RESP_ENTER_SENCE:
                size = buff.readByte();
                cargoList = new ArrayList<>(size);
                for(byte i=1;i<=size;i++){
                    po = new CargoPo();
                    po.readFromBuff(buff);
                    cargoList.add(po);
                }
                break;
            case RESP_UPDATE_CARGO:
                size = buff.readByte();
                cargoList = new ArrayList<>(size);
                for(byte i=1;i<=size;i++){
                    po = new CargoPo();
                    po.readFromBuff(buff);
                    cargoList.add(po);
                }
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    public ClientEscortSafe() {
    }

    public ClientEscortSafe(byte subtype) {
        this.subtype = subtype;
    }

    public ClientEscortSafe(byte subtype, Collection<CargoPo> cargoList) {
        this.subtype = subtype;
        this.cargoList = new ArrayList<>(cargoList);
    }

    @Override
    public short getType() {
        return EscortPacketSet.C_ESCORT_SAFE;
    }
}
