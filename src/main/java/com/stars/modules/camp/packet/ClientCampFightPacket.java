package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.pojo.CampFightGrowUP;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class ClientCampFightPacket extends Packet {
    private short subType;
    public static final short SEND_FIGHT_MAIN_UI_DATA = 1;//下发齐楚大作战主界面 数据
    public static final short SEND_START_MATCHING = 2;//下发开始匹配通知
    public static final short SEND_CANCEL_MATCHING = 3;//下发取消匹配通知
    public static final short SEND_MATCHING_SUCCESS = 4;//下发匹配成功通知
    public static final short SEND_SCORE_RANK = 5;//下发积分排行榜
    public static final short SEND_LEVEL_UP_NOTIFY = 6;//下发升级通知
    public static final short SEND_FIGHT_END = 7;//下发战斗结算
    public static final short SEND_MY_CURRENT_SCORE = 8;//下发我的当前战斗积分
    public static final short SEND_ACTIVITY_END = 9;//下发活动结束

    private RoleCampTimesPo roleCampTimesPo;
    private List<CampFightGrowUP> campFightGrowUPList;
    private CampFightGrowUP theLevelUpGuy;
    /**
     * 秦楚大作战
     *
     * @param stageType
     * @param status
     */
    private String name;//击杀者名称
    private int mySocre;
    private Map<Integer, Integer> itemMap = new HashMap<>();

    public ClientCampFightPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampFightPacket() {
    }

    @Override
    public short getType() {
        return CampPackset.C_CAMP_FIGHT;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_FIGHT_MAIN_UI_DATA: {
                Map<Integer, Map<Integer, Integer>> campActivity2ScoreMap = CampManager.campActivity2ScoreMap;
                buff.writeInt(roleCampTimesPo.getCampFightScore());
                buff.writeInt(campActivity2ScoreMap.size());
                for (Map.Entry<Integer, Map<Integer, Integer>> entry : campActivity2ScoreMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    Integer state = roleCampTimesPo.getCampFightScoreRewardState(entry.getKey());
                    buff.writeInt(state);//1：表示已领取 0：表示未领取
                    Map<Integer, Integer> reward = entry.getValue();
                    buff.writeInt(reward.size());
                    for (Map.Entry<Integer, Integer> rewardEnter : reward.entrySet()) {
                        buff.writeInt(rewardEnter.getKey());
                        buff.writeInt(rewardEnter.getValue());
                    }
                }
            }
            break;
            case SEND_SCORE_RANK: {
                buff.writeInt(campFightGrowUPList.size());
                for (CampFightGrowUP campFightGrowUP : campFightGrowUPList) {
                    buff.writeString(campFightGrowUP.getFightUid());
                    buff.writeString(campFightGrowUP.getName());
                    buff.writeString(campFightGrowUP.getServerName());
                    buff.writeInt(campFightGrowUP.getScore());
                }
            }
            break;
            case SEND_LEVEL_UP_NOTIFY: {
                buff.writeString(theLevelUpGuy.getFightUid());
                buff.writeInt(theLevelUpGuy.getLevel());
            }
            break;
            case SEND_FIGHT_END: {
                buff.writeString(name);
                buff.writeInt(mySocre);
                short size = (short) (itemMap == null ? 0 : itemMap.size());
                buff.writeShort(size);
                if (itemMap != null) {
                    for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
            }
            break;
            case SEND_MY_CURRENT_SCORE: {
                buff.writeInt(mySocre);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }


    public RoleCampTimesPo getRoleCampTimesPo() {
        return roleCampTimesPo;
    }

    public void setRoleCampTimesPo(RoleCampTimesPo roleCampTimesPo) {
        this.roleCampTimesPo = roleCampTimesPo;
    }

    public void setTheLevelUpGuy(CampFightGrowUP theLevelUpGuy) {
        this.theLevelUpGuy = theLevelUpGuy;
    }

    public List<CampFightGrowUP> getCampFightGrowUPList() {
        return campFightGrowUPList;
    }

    public void setCampFightGrowUPList(List<CampFightGrowUP> campFightGrowUPList) {
        this.campFightGrowUPList = campFightGrowUPList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMySocre() {
        return mySocre;
    }

    public void setMySocre(int mySocre) {
        this.mySocre = mySocre;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }
}
