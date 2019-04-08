package com.stars.modules.callboss.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.modules.callboss.prodata.CallBossVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/6.
 */
public class ClientCallBossVo extends PlayerPacket {
    private Map<Integer, CallBossVo> map;

    public ClientCallBossVo() {
    }

    public ClientCallBossVo(Map<Integer, CallBossVo> map) {
        this.map = map;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return CallBossPacketSet.C_CALLBOSSVO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) (map == null ? 0 : map.size());
        buff.writeByte(size);
        if (size > 0) {
            for (CallBossVo callBossVo : map.values()) {
                callBossVo.writeToBuff(buff);
            }
        }
    }
}
