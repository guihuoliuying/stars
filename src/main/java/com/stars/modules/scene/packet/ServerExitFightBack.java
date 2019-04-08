package com.stars.modules.scene.packet;

import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 由其他服务器传入服务器的离开战斗场景请求;
 * Created by panzhenfeng on 2016/10/25.
 */
public class ServerExitFightBack  extends Packet {
    private String key;

    public ServerExitFightBack(){

    }

    public ServerExitFightBack(long roleId, String key){
        setRoleId(roleId);
        this.key = key;
    }

    @Override
    public short getType() {
        return ScenePacketSet.C_EXITFIGHT_BACK;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeLong(this.getRoleId());
        buff.writeString(this.key);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        setRoleId(buff.readLong());
        this.key = buff.readString();
    }

    @Override
    public void execPacket() {

    }

    public String getKey() {
        return key;
    }
}
