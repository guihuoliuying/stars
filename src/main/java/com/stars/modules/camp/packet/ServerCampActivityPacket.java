package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampPackset;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ServerCampActivityPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_OPEN_ACTIVITY_LIST = 1;//打开阵营活动列表
    public static final short REQ_JOIN_ACTIVITY = 2;//参与阵营活动
    private int activityId;

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        switch (subType) {
            case REQ_OPEN_ACTIVITY_LIST: {
                module.reqActivityList();
            }
            break;
            case REQ_JOIN_ACTIVITY: {
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_JOIN_ACTIVITY: {
                activityId = buff.readInt();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return CampPackset.S_ACTIVITY;
    }
}
