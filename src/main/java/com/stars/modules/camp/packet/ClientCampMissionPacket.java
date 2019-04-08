package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.prodata.CampMissionVo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ClientCampMissionPacket extends Packet {
    private short subType;
    public static final short SEND_OPENED_MISSION_LIST = 1;//下发任务列表
    private List<CampMissionVo> campMissionVoList = null;
    private RoleCampTimesPo roleCampTimes;

    public ClientCampMissionPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampMissionPacket() {
    }

    @Override
    public void execPacket() {

    }

    @Override
    public short getType() {
        return CampPackset.C_MISSION;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_OPENED_MISSION_LIST:
                writeOpenedActivityList(buff);
                break;
        }
    }

    /**
     * 任务列表
     *
     * @param buff
     */
    private void writeOpenedActivityList(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(campMissionVoList.size());
        for (CampMissionVo campMissionVo : campMissionVoList) {
            int times = roleCampTimes.getJoinTimesByMisId(campMissionVo.getId());
            buff.writeByte((byte) (roleCampTimes.canGet(campMissionVo.getId()) ? 1 : 0));
            buff.writeInt(times);//任务目标达成相关次数
            campMissionVo.writeBuff(buff);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    public List<CampMissionVo> getCampMissionVoList() {
        return campMissionVoList;
    }

    public void setCampMissionVoList(List<CampMissionVo> campMissionVoList) {
        this.campMissionVoList = campMissionVoList;
    }

    public RoleCampTimesPo getRoleCampTimes() {
        return roleCampTimes;
    }

    public void setRoleCampTimes(RoleCampTimesPo roleCampTimes) {
        this.roleCampTimes = roleCampTimes;
    }
}
