package com.stars.modules.optionalbox.packet;

import com.stars.modules.optionalbox.OptionalBoxPacketSet;
import com.stars.modules.optionalbox.prodata.OptionalBox;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class ClientOptionalBoxPacket extends Packet {
    private short subType;
    public static final short SEND_TOOLLIST = 1;//下发道具列表
    public static final short SEND_CHOOSETOOL = 2;//下发选择道具结果
    private List<OptionalBox> optionalBoxes;

    public ClientOptionalBoxPacket(short subType) {
        this.subType = subType;
    }

    public ClientOptionalBoxPacket() {
    }

    @Override
    public short getType() {
        return OptionalBoxPacketSet.C_TOOLCHOOSE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_TOOLLIST: {
                buff.writeInt(optionalBoxes.size());
                for (OptionalBox optionalBox : optionalBoxes) {
                    optionalBox.writeBuff(buff);
                }
            }
            break;
            case SEND_CHOOSETOOL: {

            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
    }

    @Override
    public void execPacket() {

    }

    public void setOptionalBoxes(List<OptionalBox> optionalBoxes) {
        this.optionalBoxes = optionalBoxes;
    }
}
