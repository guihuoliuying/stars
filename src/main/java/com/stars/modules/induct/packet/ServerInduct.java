package com.stars.modules.induct.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.induct.InductModule;
import com.stars.modules.induct.InductPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class ServerInduct extends PlayerPacket {
    public final static byte REQ_TYPE_TRIGGER = 1; //请求触发引导
    public final static byte REQ_TYPE_FINISHED = 2; //请求完成引导
    public final static byte REQ_TYPE_FINISHED_FORCE = 3; //请求强制完成引导,无论是否有触发;

    private byte reqType;// 请求类型(子协议)

    private int inductId;

    @Override
    public void execPacket(Player player) {
        InductModule inductModule = (InductModule) module(MConst.Induct);
        switch (reqType) {
            case REQ_TYPE_TRIGGER:// 触发引导
                inductModule.triggerUpdate(inductId);
                break;
            case REQ_TYPE_FINISHED:// 完成引导
                inductModule.finish(inductId);
                break;
            case REQ_TYPE_FINISHED_FORCE:// 强制完成引导
                inductModule.forceFinish(inductId);
                ClientInduct packet = new ClientInduct(ClientInduct.RESPONSE_NONE_INDUCT);
                packet.setInductId(inductId);
                PlayerUtil.send(getRoleId(), packet);
                break;
            default:
                break;
        }
        ClientInduct noneClientInduct = new ClientInduct(ClientInduct.RESPONSE_NONE);
        PlayerUtil.send(getRoleId(), noneClientInduct);
    }

    @Override
    public short getType() {
        return InductPacketSet.S_INDUCT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case REQ_TYPE_TRIGGER:// 触发引导
                this.inductId = buff.readInt();
                break;
            case REQ_TYPE_FINISHED:// 完成引导
                this.inductId = buff.readInt();
                break;
            case REQ_TYPE_FINISHED_FORCE:// 完成引导
                this.inductId = buff.readInt();
                break;
            default:
                break;
        }
    }
    
    public byte getReqType(){
    	return this.reqType;
    }
    
    public int getInductId(){
    	return this.inductId;
    }
}
