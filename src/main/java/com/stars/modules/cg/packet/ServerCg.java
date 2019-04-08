package com.stars.modules.cg.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.cg.CgModule;
import com.stars.modules.cg.CgPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class ServerCg extends PlayerPacket {
    public final static byte REQ_TYPE_FINISHED = 1; //请求完成CG

    private byte reqType;// 请求类型(子协议)

    private String cgId;

    @Override
    public void execPacket(Player player) {
        CgModule cgModule = (CgModule) module(MConst.Cg);
        switch (reqType) {
            case REQ_TYPE_FINISHED:// 完成CG
                cgModule.setFinished(cgId);
                break;
        }
        ClientCg clientCg = new ClientCg();
        PlayerUtil.send(getRoleId(), clientCg);
    }

    @Override
    public short getType() {
        return CgPacketSet.S_CG;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case REQ_TYPE_FINISHED:// 完成引导
                this.cgId = buff.readString();
                break;
        }
    }
}
