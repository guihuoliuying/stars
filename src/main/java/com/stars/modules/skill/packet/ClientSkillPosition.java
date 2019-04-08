package com.stars.modules.skill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Iterator;
import java.util.Map;

public class ClientSkillPosition extends PlayerPacket {

    private Map<Integer, Byte> map;

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public short getType() {
        return SkillPacketSet.Client_Skill_Position;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int size = map.size();
        buff.writeByte((byte) size);
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            int id = it.next();
            buff.writeInt(id);
            buff.writeByte(map.get(id));
        }
        LogUtil.info("client skillMap:{}", map);
    }

    public Map<Integer, Byte> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Byte> map) {
        this.map = map;
    }
}
