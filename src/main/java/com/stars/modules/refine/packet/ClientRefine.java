package com.stars.modules.refine.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.refine.RefineManager;
import com.stars.modules.refine.RefinePacketSet;
import com.stars.modules.refine.cache.RoleRefine;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class ClientRefine extends PlayerPacket {
    private Map<Integer, RoleRefine> roleRefineMap = new HashMap<>();

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(roleRefineMap.size());
        for (RoleRefine refine : roleRefineMap.values()) {
            refine.writeToBuff(buff);
            buff.writeString(RefineManager.getOutput(refine.getItemId()));
            buff.writeInt(RefineManager.getOrder(refine.getItemId()));
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RefinePacketSet.C_REFINE;
    }

    public void setRoleRefineMap(Map<Integer, RoleRefine> roleRefineMap) {
        this.roleRefineMap = roleRefineMap;
    }
}
