package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.prodata.*;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class ClientCampPacket extends Packet {
    private short subType;
    public static final short SEND_MY_CAMP_STATE = 0;//我的阵营状态
    public static final short SEND_ALL_CAMP_INFO = 1;//下发所有阵营信息(产品数据)
    public static final short SEND_CURRENT_CAMP_INFO = 2;//下发当前阵营负载情况
    public static final short SEND_MY_CAMP_INFO = 3;//下发我的阵营情况
    public static final short SEND_JOIN_SUCCESS = 4;//加入成功
    private RoleCampPo roleCampPo;
    private CampTypeScale campTypeScale;
    private AllServerCampPo allServerCamp;
    private Map<Integer, AllServerCampPo> allServerCampPoMap;

    public ClientCampPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampPacket() {
    }

    @Override
    public short getType() {
        return CampPackset.C_CAMP;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MY_CAMP_STATE: {
                if (roleCampPo == null) {
                    buff.writeInt(0);//没有阵营
                } else {
                    buff.writeInt(1);//有阵营
                }
            }
            break;
            case SEND_ALL_CAMP_INFO: {
                for (CampAtrVo campAtrVo : CampManager.campAtrMap.values()) {
                    campAtrVo.writeBuff(buff);
                }
                /**
                 * 随机进入奖励
                 */
                buff.writeInt(CampManager.randomEnterReward.size());
                for (Map.Entry<Integer, Integer> entry : CampManager.randomEnterReward.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                /**
                 * 普通官职数据
                 */
                buff.writeInt(CampManager.commonOfficerVoList.size());
                for (CommonOfficerVo commonOfficerVo : CampManager.commonOfficerVoList) {
                    commonOfficerVo.writeBuff(buff);
                }
                /**
                 * 稀有官职数据
                 */
                buff.writeInt(CampManager.rareOfficerMap.size());
                for (RareOfficerVo rareOfficerVo : CampManager.rareOfficerMap.values()) {
                    rareOfficerVo.writeBuff(buff);
                }
                /**
                 * 城池数据
                 */
                buff.writeInt(CampManager.campCityVoList.size());
                for (CampCityVo campCityVo : CampManager.campCityVoList) {
                    campCityVo.writeBuff(buff);
                }
            }
            break;
            case SEND_CURRENT_CAMP_INFO: {
                writeCurrentCampInfo(buff);
            }
            break;
            case SEND_MY_CAMP_INFO: {
                writeMyMainCampInfo(buff);
            }
            break;

        }
    }

    /**
     * 我的阵营主界面数据
     *
     * @param buff
     */
    private void writeMyMainCampInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(allServerCamp.getCampType());
        buff.writeString(allServerCamp.getCampLevelVo().getImage());
        buff.writeInt(allServerCamp.getLevel());
        buff.writeLong(allServerCamp.getProsperousnum());//繁荣度
        CampLevelVo campLevelVo = CampManager.getCampLevelVo(allServerCamp.getCampType(), allServerCamp.getLevel());
        buff.writeLong(campLevelVo.getReqlevel());//繁荣度最大值
        buff.writeInt(allServerCamp.getOpenCityIdList().size());//开放城池数量
        buff.writeInt(allServerCamp.getRoleNum());
        int theCityId = ServiceHelper.campLocalMainService().getHighestRareOfficerCityId(allServerCamp.getCampType());
        /**
         * 获取当前开放的最高等级城池
         */
        ClientOfficerPacket.writeTheCityRareOfficerBuff(theCityId, buff);
    }

    /**
     * 下发当前阵营负载信息提示和奖励
     *
     * @param buff
     */
    private void writeCurrentCampInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(allServerCampPoMap.size());
        CampEquilibrium campEquilibrium = campTypeScale.getCampEquilibrium();
        for (Map.Entry<Integer, AllServerCampPo> entry : allServerCampPoMap.entrySet()) {
            Integer campType = entry.getKey();
            AllServerCampPo allServerCampPo = entry.getValue();
            buff.writeInt(campType);//阵营id
            buff.writeInt(allServerCampPo.getRoleNum());//阵营角色数量
            if (campEquilibrium != null) {
                if (campType == campTypeScale.getLowCampType()) {
                    buff.writeString(campEquilibrium.getMinDesc());//人数少的一方的文本提示，gametext key
                } else {
                    buff.writeString(campEquilibrium.getMaxDesc());//人数多的一方的文本提示，gametext key
                }
            } else {
                buff.writeString("");
            }
        }
        if (campEquilibrium != null) {
            buff.writeInt(campEquilibrium.getReward().size());
            for (Map.Entry<Integer, Integer> entry : campEquilibrium.getReward().entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        } else {
            buff.writeInt(0);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public CampTypeScale getCampTypeScale() {
        return campTypeScale;
    }

    public void setCampTypeScale(CampTypeScale campTypeScale) {
        this.campTypeScale = campTypeScale;
    }

    public AllServerCampPo getAllServerCamp() {
        return allServerCamp;
    }

    public void setAllServerCamp(AllServerCampPo allServerCamp) {
        this.allServerCamp = allServerCamp;
    }

    public RoleCampPo getRoleCampPo() {
        return roleCampPo;
    }

    public void setRoleCampPo(RoleCampPo roleCampPo) {
        this.roleCampPo = roleCampPo;
    }

    public Map<Integer, AllServerCampPo> getAllServerCampPoMap() {
        return allServerCampPoMap;
    }

    public void setAllServerCampPoMap(Map<Integer, AllServerCampPo> allServerCampPoMap) {
        this.allServerCampPoMap = allServerCampPoMap;
    }
}
