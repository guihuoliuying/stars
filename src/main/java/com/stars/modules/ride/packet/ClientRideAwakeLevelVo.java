package com.stars.modules.ride.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.ride.RideManager;
import com.stars.modules.ride.RidePacketSet;
import com.stars.modules.ride.prodata.RideAwakeLvlVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/7/5.
 */
public class ClientRideAwakeLevelVo extends PlayerPacket {

    private List<int[]> rideAwakeLevelList; // int[] { rideId, awakeLevel }

    public ClientRideAwakeLevelVo() {
        this.rideAwakeLevelList = new ArrayList<>();
    }

    @Override
    public short getType() {
        return RidePacketSet.C_AWAKE_LEVEL;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        // calc
        List<RideAwakeLvlVo> syncVoList = new ArrayList<>();
        for (int[] pair : rideAwakeLevelList) {
            RideAwakeLvlVo currLevelVo = RideManager.getRideAwakeLvlVo(pair[0], pair[1]);
            RideAwakeLvlVo nextLevelVo = RideManager.getRideAwakeLvlVo(pair[0], pair[1]+1);
            if (currLevelVo != null) syncVoList.add(currLevelVo);
            if (nextLevelVo != null) syncVoList.add(nextLevelVo);
        }
        // sync
        buff.writeInt(syncVoList.size());
        for (RideAwakeLvlVo vo : syncVoList) {
            vo.writeToBuffer(buff);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    public void add(int rideId, int awakeLevel) {
        this.rideAwakeLevelList.add(new int[] { rideId, awakeLevel });
    }
}
