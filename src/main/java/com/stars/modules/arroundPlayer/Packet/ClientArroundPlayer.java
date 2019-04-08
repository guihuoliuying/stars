package com.stars.modules.arroundPlayer.Packet;


import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.arroundPlayer.ArroundPlayerPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ArroundPlayer.ArroundServiceActor;

public class ClientArroundPlayer extends Packet {

    private ArroundPlayer[] apl;

    private byte index = 0;

    public ClientArroundPlayer() {

    }

    public ClientArroundPlayer(int initCapacity) {
        apl = new ArroundPlayer[initCapacity];
    }

    public void addArroundPlayer(ArroundPlayer ap) {
        if (index >= ArroundServiceActor.SEND_ARROUNDPLAYER_COUNT) {
            return;
        }
        apl[index] = ap;
        index++;
    }

    @Override
    public short getType() {
        return ArroundPlayerPacketSet.Client_ArroundPlayer;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(index);
        for (int i = 0; i < index; i++) {
            buff.writeString(String.valueOf(apl[i].getRoleId()));
            buff.writeString(apl[i].getName());
            buff.writeShort(apl[i].getLevel());
            buff.writeByte((byte) apl[i].getJob());
            buff.writeInt(apl[i].getActiveRideId()); // 骑乘的坐骑id
            buff.writeInt(apl[i].getX());
            buff.writeInt(apl[i].getY());
            buff.writeInt(apl[i].getZ());
            buff.writeInt(apl[i].getCurFashionId());//穿着的时装id，无穿为-1
            buff.writeByte(apl[i].getDeityweaponType());//神兵类型;
            buff.writeInt(apl[i].getCurTitleId());// 角色装上的称号Id,没有则发0
            buff.writeInt(apl[i].getCutVipLevel());// 角色当前vip等级
            buff.writeString(apl[i].getFamilyId());
            buff.writeString(apl[i].getOriginSceneId());
            buff.writeInt(apl[i].getFightScore());
            if (null != apl[i].getDragonBallList()) {
                buff.writeInt(apl[i].getDragonBallList().size()); //龙珠数量
                for (String dragonBallId : apl[i].getDragonBallList()) {
                    buff.writeString(dragonBallId); //龙珠ID
                }
            } else {
                buff.writeInt(0);
            }
            buff.writeByte(apl[i].getBabyFollow());
            buff.writeInt(apl[i].getBabyCurFashionId());
            buff.writeInt(apl[i].getCurFashionCardId());
            apl[i] = null;

        }
        index = 0;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        // TODO Auto-generated method stub

    }

    @Override
    public void execPacket() {
        // TODO Auto-generated method stub

    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }
}
