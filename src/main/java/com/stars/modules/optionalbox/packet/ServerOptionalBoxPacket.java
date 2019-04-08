package com.stars.modules.optionalbox.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.optionalbox.OptionalBoxModule;
import com.stars.modules.optionalbox.OptionalBoxPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class ServerOptionalBoxPacket extends PlayerPacket {
    private short subType;
    private int itemId;
    private int optionId;
    private int count;
    public static final short REQ_TOOLlIST = 1;//请求可选列表
    public static final short REQ_CHOOSEITEM = 2;//请求选定指定物品

    @Override
    public void execPacket(Player player) {
        OptionalBoxModule optionalBoxModule = module(MConst.OptionalBox);
        switch (subType) {
            case REQ_TOOLlIST: {
                optionalBoxModule.reqToolList(itemId, count);//可选礼包itemid
            }
            break;
            case REQ_CHOOSEITEM: {
                optionalBoxModule.reqChooseItem(itemId, count, optionId);//选定的id
            }
            break;
        }
    }

    @Override
    public short getType() {
        return OptionalBoxPacketSet.S_TOOLCHOOSE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_TOOLlIST: {
                itemId = buff.readInt();
                count = buff.readInt();

            }
            break;
            case REQ_CHOOSEITEM: {
                optionId = buff.readInt();
                count = buff.readInt();
                itemId = buff.readInt();
            }
            break;
        }
    }
}
