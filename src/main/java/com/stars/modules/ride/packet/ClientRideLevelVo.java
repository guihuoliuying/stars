package com.stars.modules.ride.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.ride.RideManager;
import com.stars.modules.ride.RidePacketSet;
import com.stars.modules.ride.prodata.RideLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/19.
 */
public class ClientRideLevelVo extends PlayerPacket {

    private List<Integer> list = new LinkedList<>();

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RidePacketSet.C_LEVEL;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        Map<Integer, RideLevelVo> rideLevelVoMap = RideManager.rideLevelIdMap;
        buff.writeByte((byte) list.size());
        for (Integer levelId : list) {
            RideLevelVo levelVo = rideLevelVoMap.get(levelId);
            levelVo.writeToBuffer(buff);
        }
    }

    public void add(int levelId) {
        Map<Integer, RideLevelVo> rideLevelVoMap = RideManager.rideLevelIdMap;
        if (rideLevelVoMap.get(levelId) != null) {
            list.add(levelId);
        }
    }
}
