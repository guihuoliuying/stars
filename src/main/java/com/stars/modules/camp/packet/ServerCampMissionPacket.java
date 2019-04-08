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
public class ServerCampMissionPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_OPEN_MISSION_LIST = 1;//打开阵营任务列表
    public static final short REQ_ENTER_SHOU_HU_QU_YUAN = 2;//进入守护屈原
    public static final short REQ_GET_AWARD = 3;
    private byte teamType;
    private int stageId;
    private int missionId;

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        switch (subType) {
            case REQ_OPEN_MISSION_LIST:
                module.reqMissionList();
                break;
            case REQ_ENTER_SHOU_HU_QU_YUAN:
                break;
            case REQ_GET_AWARD:
                module.getMissionAward(missionId);
                break;

        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_ENTER_SHOU_HU_QU_YUAN:
                teamType = buff.readByte();
                stageId = buff.readInt();
                break;
            case REQ_GET_AWARD:
                missionId = buff.readInt();
                break;
        }
    }

    @Override
    public short getType() {
        return CampPackset.S_MISSION;
    }

    public byte getTeamType() {
        return teamType;
    }

    public int getMissionId() {
        return missionId;
    }

    public int getStageId() {
        return stageId;
    }

}
