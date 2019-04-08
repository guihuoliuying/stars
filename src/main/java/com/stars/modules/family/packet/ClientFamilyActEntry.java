package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.activities.entry.FamilyActEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/10.
 */
public class ClientFamilyActEntry extends PlayerPacket {

    private Map<Integer, FamilyActEntry> entryMap;
    private Map<Integer, Integer> maskMap;
    private List<Integer> notShowList;

    public ClientFamilyActEntry() {
    }

    public ClientFamilyActEntry(Map<Integer, FamilyActEntry> entryMap, Map<Integer, Integer> maskMap, List<Integer> notShowList) {
        this.entryMap = entryMap;
        this.maskMap = maskMap;
        this.notShowList = notShowList;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_ACT_ENTRY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        List<FamilyActEntry> sendList = new ArrayList<>();
        for (FamilyActEntry entry : entryMap.values()) {
            if (notShowList.contains(entry.getActivityId())) continue;
            sendList.add(entry);
        }
        buff.writeByte((byte) sendList.size());
        for (FamilyActEntry entry : sendList) {
            entry.writeToBuffer(buff,
                    maskMap.containsKey(entry.getActivityId())
                            ? maskMap.get(entry.getActivityId()) : entry.getFlag());
        }
    }

}
