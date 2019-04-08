package com.stars.modules.quwudu.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.quwudu.QuwuduModule;
import com.stars.modules.quwudu.QuwuduPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/5/18.
 */
public class ServerQuwuduPacket extends PlayerPacket {
    private byte subType;
    private static final byte REQ_LIMIT_TIME = 1;//请求副本次数
    private static final byte REQ_ENTER_SCENE = 2; //请求进入副本


    @Override
    public void execPacket(Player player) {
        QuwuduModule module = module(MConst.Quwudu);
        switch (subType) {
            case REQ_LIMIT_TIME: {
                module.sendTime();

            }
            break;
            case REQ_ENTER_SCENE: {
                module.enterScene();
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
    }

    @Override
    public short getType() {
        return QuwuduPacketSet.S_QUWUDU;
    }
}
