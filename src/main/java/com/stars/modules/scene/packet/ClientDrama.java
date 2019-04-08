package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.userdata.RoleDrama;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Collection;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class ClientDrama extends PlayerPacket {
    private byte sendType;// 下发类型

    /* 子协议 */
    public static final byte PLAYED_DRAMA = 1;// 已播放剧情

    /* 参数 */
    private String type;// 类型
    private Collection<RoleDrama> roleDramas;

    public ClientDrama() {
    }

    public ClientDrama(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_DRAMA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case PLAYED_DRAMA:
                buff.writeString(type);// 剧情类型
                short size = (short) (roleDramas == null ? 0 : roleDramas.size());
                buff.writeShort(size);
                if (size == 0)
                    return;
                for (RoleDrama roleDrama : roleDramas) {
                    buff.writeString(roleDrama.getParamId());// 条目Id
                    byte dramaSize = (byte) roleDrama.getDramaSet().size();
                    buff.writeByte(dramaSize);
                    for (String dramaId : roleDrama.getDramaSet()) {
                        buff.writeString(dramaId);// 剧情Id
                    }
                }
                break;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRoleDramas(Collection<RoleDrama> roleDramas) {
        this.roleDramas = roleDramas;
    }
}
