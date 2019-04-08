package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.prodata.CampLevelVo;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ClientCampCityPacket extends Packet {
    private short subType;
    public static final short SEND_JOIN_CITY_SUCCESS = 1;//入驻城池成功
    public static final short SEND_OPENED_CAMP_CITIES = 2;//发送已经开启的阵营城池id
    public static final short SEND_THE_CITY_RANK = 3;//发送指定城池的排行榜
    private List<CampRoleReputationRankPo> reputationRankPoList;
    private Map<Integer, AllServerCampPo> allServerCampMap;
    private CampTypeScale campTypeScale;
    private RoleCampPo roleCampPo;
    private boolean hasOwn;

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
    }

    public ClientCampCityPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampCityPacket() {
    }

    @Override
    public void execPacket() {

    }

    @Override
    public short getType() {
        return CampPackset.C_CITY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_OPENED_CAMP_CITIES: {
                buff.writeInt(roleCampPo.getCommonOfficerId());
                buff.writeInt(roleCampPo.getCampType());
                buff.writeInt(roleCampPo.getCityId());
                buff.writeInt(allServerCampMap.size());
                for (AllServerCampPo allServerCampPo : allServerCampMap.values()) {
                    Integer campType = allServerCampPo.getCampType();
                    buff.writeInt(campType);
                    List<Integer> openCityIdList = allServerCampPo.getOpenCityIdList();
                    buff.writeInt(allServerCampPo.getRoleNum());
                    CampLevelVo campLevelVo = allServerCampPo.getCampLevelVo();
                    buff.writeString(campLevelVo.getImage());
                    buff.writeInt(allServerCampPo.getLevel());
                    CampEquilibrium campEquilibrium = campTypeScale.getCampEquilibrium();
                    if (campEquilibrium != null) {
                        if (campType == campTypeScale.getLowCampType()) {
                            buff.writeString(campEquilibrium.getMinDesc());
                        } else {
                            buff.writeString(campEquilibrium.getMaxDesc());
                        }
                    } else {
                        buff.writeString("");
                    }
                    buff.writeInt(openCityIdList.size());
                    for (int cityId : openCityIdList) {
                        buff.writeInt(cityId);
                    }
                }
            }
            break;
            case SEND_THE_CITY_RANK: {
                buff.writeInt(reputationRankPoList.size());
                for (CampRoleReputationRankPo rankPo : reputationRankPoList) {
                    rankPo.writeToBuffer(0, buff);
                }
                if (hasOwn) {
                    buff.writeByte((byte) 1);

                } else {
                    buff.writeByte((byte) 0);
                }
            }
            break;
        }
    }


    public List<CampRoleReputationRankPo> getReputationRankPoList() {
        return reputationRankPoList;
    }

    public void setReputationRankPoList(List<CampRoleReputationRankPo> reputationRankPoList) {
        this.reputationRankPoList = reputationRankPoList;
    }

    public Map<Integer, AllServerCampPo> getAllServerCampMap() {
        return allServerCampMap;
    }

    public void setAllServerCampMap(Map<Integer, AllServerCampPo> allServerCampMap) {
        this.allServerCampMap = allServerCampMap;
    }

    public CampTypeScale getCampTypeScale() {
        return campTypeScale;
    }

    public void setCampTypeScale(CampTypeScale campTypeScale) {
        this.campTypeScale = campTypeScale;
    }

    public RoleCampPo getRoleCampPo() {
        return roleCampPo;
    }

    public void setRoleCampPo(RoleCampPo roleCampPo) {
        this.roleCampPo = roleCampPo;
    }

    public boolean isHasOwn() {
        return hasOwn;
    }

    public void setHasOwn(boolean hasOwn) {
        this.hasOwn = hasOwn;
    }
}
