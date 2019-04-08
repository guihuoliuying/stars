package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.prodata.CommonOfficerVo;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.multiserver.camp.usrdata.RareOfficerRolePo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;

import java.util.*;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ClientOfficerPacket extends Packet {
    private short subType;
    public static final short SEND_MY_CAMP_OFFICER = 2;//发送我的阵营官职(阵营聊天)
    public static final short SEND_THE_CITY_RARE_OFFICER = 3;//发送某城池稀有官职(用于天下大势)
    public static final short SEND_RARE_OFFICER_NOTICE = 4;//客户端弹窗提示获得新稀有官职
    public static final short SEND_MAIN_OFFICER_UI = 5;//官职一览下发界面数据
    public static final short SEND_OFFICER_UPGRADE_UI = 6;//官职升级下发界面数据
    public static final short SEND_OFFICER_UPGRADE_SUCCESS = 7;//官职升级成功
    public static final short SEND_TAKE_REWARD_STATE = 8;//下发领奖状态
    public static final short SEND_DONATE_YB_UI = 9;//下发捐献元宝界面数据
    private RoleCampPo roleCampPo;
    private RoleCampTimesPo roleCampTimes;
    private int fightScore;
    private int featsNum;//功勋数量
    private int reputationNum;//声望数
    private List<RareOfficerRolePo> rareOfficerRoleList;
    private int theCityId;
    private int dayDonateYBCount;

    public ClientOfficerPacket(short subType) {
        this.subType = subType;
    }

    public ClientOfficerPacket() {
    }

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    @Override
    public short getType() {
        return CampPackset.C_OFFICER;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MY_CAMP_OFFICER: {
                buff.writeInt(roleCampPo.getCampType());
                buff.writeInt(roleCampPo.getCommonOfficerId());
                buff.writeInt(roleCampPo.getRareOfficerId());//无稀有官职为0
                buff.writeInt(roleCampPo.getDesignateOfficerId());
                buff.writeInt(roleCampPo.getCityId());
            }
            break;
            case SEND_THE_CITY_RARE_OFFICER: {
                buff.writeInt(theCityId);
                writeTheCityRareOfficerBuff(theCityId, buff);
            }
            break;
            case SEND_RARE_OFFICER_NOTICE: {
                buff.writeInt(roleCampPo.getRareOfficerId());
            }
            break;
            case SEND_MAIN_OFFICER_UI: {
                writeMainOfficerUI(buff);
            }
            break;
            case SEND_OFFICER_UPGRADE_UI: {
                writeOfficerUpgradeUI(buff);
            }
            break;
            case SEND_TAKE_REWARD_STATE: {
                buff.writeInt(roleCampTimes.getDailyRewardTimes());
            }
            break;
            case SEND_DONATE_YB_UI: {
                buff.writeInt(dayDonateYBCount);//本日可捐献额度
            }
            break;
        }
    }

    /**
     * 升级界面数据
     *
     * @param buff
     */
    private void writeOfficerUpgradeUI(com.stars.network.server.buffer.NewByteBuffer buff) {
        int commonOfficerId = roleCampPo.getCommonOfficerId();
        CommonOfficerVo commonOfficerVo = CampManager.commonOfficerMap.get(commonOfficerId);
        CommonOfficerVo nextLevelCommonOfficerVo = commonOfficerVo.getNextLevelCommonOfficerVo();
        if (nextLevelCommonOfficerVo == null) {
            nextLevelCommonOfficerVo = commonOfficerVo;
        }
        buff.writeInt(featsNum);
        buff.writeInt(commonOfficerId);
        buff.writeInt(nextLevelCommonOfficerVo.getId());
        int cityId = roleCampPo.getCityId();
        CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
        if (campCityVo.canJoin(nextLevelCommonOfficerVo.getLevel())) {
            buff.writeString("");
        } else {
            CampCityVo nextLevelCity = campCityVo.getNextLevelCity();
            buff.writeString(nextLevelCity.getName());
        }
    }

    /**
     * 官职一览
     *
     * @param buff
     */
    private void writeMainOfficerUI(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(featsNum);//功勋
        buff.writeInt(reputationNum);//声望
        buff.writeInt(roleCampPo.getCommonOfficerId());
        buff.writeInt(roleCampPo.getRareOfficerId());
        buff.writeInt(roleCampPo.getDesignateOfficerId());
        buff.writeInt(roleCampPo.getCityId());
        buff.writeInt(fightScore);
//        buff.writeInt(featsNum);
        Integer dailyRewardTimes = roleCampTimes.getDailyRewardTimes();
        buff.writeInt(dailyRewardTimes);//0：未领取，1：已领取
        writeTheCityRareOfficerBuff(roleCampPo.getCityId(), buff);

    }

    /**
     * 指定城池的稀有官职相关信息
     *
     * @param cityId
     * @param buff
     */
    public static void writeTheCityRareOfficerBuff(int cityId, NewByteBuffer buff) {
        /**
         * 获取指定的城池
         */
        CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
        Map<Integer, Integer> rareOfficerMap = campCityVo.getRareOfficerMap();
        List<Integer> rareOfficerList = new ArrayList<>(rareOfficerMap.keySet());
        Collections.sort(rareOfficerList, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        buff.writeInt(rareOfficerList.size());
        for (Integer officerId : rareOfficerList) {
            buff.writeInt(officerId);
        }
        List<RareOfficerRolePo> allRareOfficerList = ServiceHelper.campLocalMainService().getCurrentRoundRoleRareOfficerListByCityId(cityId);
        if (allRareOfficerList != null) {
            /**
             * 当前城池最高官职
             */
            buff.writeInt(allRareOfficerList.size());
            for (RareOfficerRolePo rareOfficerRole : allRareOfficerList) {
                rareOfficerRole.writeBuff(buff);
            }
        } else {
            buff.writeInt(0);
        }
    }

    public RoleCampPo getRoleCampPo() {
        return roleCampPo;
    }

    public void setRoleCampPo(RoleCampPo roleCampPo) {
        this.roleCampPo = roleCampPo;
    }

    public List<RareOfficerRolePo> getRareOfficerRoleList() {
        return rareOfficerRoleList;
    }

    public void setRareOfficerRoleList(List<RareOfficerRolePo> rareOfficerRoleList) {
        this.rareOfficerRoleList = rareOfficerRoleList;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getTheCityId() {
        return theCityId;
    }

    public void setTheCityId(int theCityId) {
        this.theCityId = theCityId;
    }

    public RoleCampTimesPo getRoleCampTimes() {
        return roleCampTimes;
    }

    public void setRoleCampTimes(RoleCampTimesPo roleCampTimes) {
        this.roleCampTimes = roleCampTimes;
    }

    public int getFeatsNum() {
        return featsNum;
    }

    public void setFeatsNum(int featsNum) {
        this.featsNum = featsNum;
    }

    public int getReputationNum() {
        return reputationNum;
    }

    public void setReputationNum(int reputationNum) {
        this.reputationNum = reputationNum;
    }

    public int getDayDonateYBCount() {
        return dayDonateYBCount;
    }

    public void setDayDonateYBCount(int dayDonateYBCount) {
        this.dayDonateYBCount = dayDonateYBCount;
    }
}
