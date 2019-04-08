package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.prodata.CampVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/31.
 */
public class ClientCampVo extends PlayerPacket {
    Map<String, CampVo> campVoMap;

    public ClientCampVo() {
    }

    public ClientCampVo(Map<String, CampVo> campVoMap) {
        this.campVoMap = campVoMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_CAMPVO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) (campVoMap == null ? 0 : campVoMap.size());
        buff.writeByte(size);
        if (size == 0) return;
        for (CampVo campVo : campVoMap.values()) {
            campVo.writeToBuff(buff);
        }
    }
}
