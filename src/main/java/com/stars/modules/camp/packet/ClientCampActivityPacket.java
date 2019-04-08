package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.prodata.CampActivityVo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ClientCampActivityPacket extends Packet {
    private short subType;
    public static final short SEND_OPENED_ACTIVITY_LIST = 1;
    private List<CampActivityVo> campActivityVoList = null;
    private RoleCampTimesPo roleCampTimes;

    public ClientCampActivityPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampActivityPacket() {
    }

    @Override
    public void execPacket() {

    }

    @Override
    public short getType() {
        return CampPackset.C_ACTIVITY;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_OPENED_ACTIVITY_LIST: {
                writeOpenedActivityList(buff);
            }
            break;
        }
    }

    private void writeOpenedActivityList(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(campActivityVoList.size());
        for (CampActivityVo campActivityVo : campActivityVoList) {
            Integer times = roleCampTimes.getJoinTimesByActId(campActivityVo.getId());
            buff.writeInt(times);//本活动今日参与的次数
            campActivityVo.writeBuff(buff);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    public List<CampActivityVo> getCampActivityVoList() {
        return campActivityVoList;
    }

    public void setCampActivityVoList(List<CampActivityVo> campActivityVoList) {
        this.campActivityVoList = campActivityVoList;
    }

    public RoleCampTimesPo getRoleCampTimes() {
        return roleCampTimes;
    }

    public void setRoleCampTimes(RoleCampTimesPo roleCampTimes) {
        this.roleCampTimes = roleCampTimes;
    }
}
