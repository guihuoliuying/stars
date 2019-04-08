package com.stars.modules.trump.packet;

import com.stars.modules.trump.TrumpPacketSet;
import com.stars.modules.trump.userdata.RoleTrumpKarma;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class ClientTrumpKarmaPacket extends Packet {
    private short subType;
    public static final short SEND_KARMA_LIST = 1;//下发仙缘列表
    public static final short SEND_ACTIVE_SUCCESS = 2;//下发激活结果
    private List<RoleTrumpKarma> roleTrumpKarmaList;
    private boolean includeProduct;

    public ClientTrumpKarmaPacket(short subType) {
        this.subType = subType;
    }

    public ClientTrumpKarmaPacket() {
    }

    @Override
    public short getType() {
        return TrumpPacketSet.CLIENT_TRUMP_KARMA;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_KARMA_LIST: {
                buff.writeInt(includeProduct?1:0);//1包含产品数据，0不包含
                buff.writeInt(roleTrumpKarmaList.size());
                for (RoleTrumpKarma roleTrumpKarma : roleTrumpKarmaList) {
                    if (includeProduct) {
                        roleTrumpKarma.getTrumpKarma().writeBuff(buff);
                    }
                    roleTrumpKarma.writeBuff(buff);
                }
            }
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public List<RoleTrumpKarma> getRoleTrumpKarmaList() {
        return roleTrumpKarmaList;
    }

    public void setRoleTrumpKarmaList(List<RoleTrumpKarma> roleTrumpKarmaList) {
        this.roleTrumpKarmaList = roleTrumpKarmaList;
    }


    public void setIncludeProduct(boolean includeProduct) {
        this.includeProduct = includeProduct;
    }
}
