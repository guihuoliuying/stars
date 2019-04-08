package com.stars.modules.daily.packet;

import com.stars.modules.daily.DailyPacketSet;
import com.stars.modules.daily.prodata.DailyBallStageVo;
import com.stars.modules.daily.userdata.RoleTagFightDelta;
import com.stars.modules.daily.userdata.RoleTmpDayInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/10.
 */
public class ClientDailyData extends Packet {

    public static final byte RESP_TAG_LIST = 0x00; //返回左标签列表
    public static final byte RESP_DAILYINFO_IN_TAG = 0x01; //返回某个标签下的活动数据
    public static final byte RESP_LUCK_DRAW = 0x02; //返回需要抽签的页面
    public static final byte RESP_DAILY_BALL = 0x03; //返回斗魂珠数据
    public static final byte RESP_MUTIPLE_OR_SUPER_AWARD = 0x04; //返回获得的奖励信息
    public static final byte RESP_DAILY_DATA_UPDATE = 0x05; //返回玩家完成情况


    private byte respType;
    private List<RoleTagFightDelta> roleTagFightDeltaList;
    private byte isSuperAwardToday; //今日是否有超级奖励
    private byte chooseTagId; //选中的tagId
    private List<RoleTmpDayInfo> roleTmpDayInfoList; //标签下的活动数据
    private DailyBallStageVo dailyBallStageVo;
    private int roleOwnDailyBallScore; //玩家目前累积的斗魂值
    private short dailyId;
    private byte showAwardType; //显示奖励类型：0-回城显示 1-立即显示
    private byte awardType;  //奖励类型
    private Map<Integer,Integer> awardMap; //奖励信息
   	private Map<Short, Integer> dailyInfoMap;


    @Override
    public short getType() {
        return DailyPacketSet.Client_DailyData;
    }

    @Override
    public void execPacket() {

    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(respType);
        switch (respType){
            case RESP_TAG_LIST:
                int size = roleTagFightDeltaList.size();
                buff.writeInt(size); //标签数据量
                for(RoleTagFightDelta roleData:roleTagFightDeltaList) {
                    buff.writeByte(roleData.getTagId()); //标签页
                }
                break;
            case RESP_DAILYINFO_IN_TAG:
                buff.writeByte(chooseTagId);
                int dailyInfoSize = roleTmpDayInfoList.size();
                buff.writeInt(dailyInfoSize);
                for(RoleTmpDayInfo roleTmpDayInfo:roleTmpDayInfoList){
                    roleTmpDayInfo.writeToBuff(buff);
                }
                break;
            case RESP_LUCK_DRAW:
                buff.writeByte(isSuperAwardToday);
                break;
            case RESP_DAILY_BALL:
                buff.writeInt(roleOwnDailyBallScore);
                dailyBallStageVo.writeToBuffer(buff);
                break;
            case RESP_MUTIPLE_OR_SUPER_AWARD:
                buff.writeShort(dailyId);
                buff.writeByte(showAwardType);
                buff.writeByte(awardType);
                int awardSize = awardMap.size();
                buff.writeInt(awardSize);
                for(Map.Entry<Integer,Integer> entry: awardMap.entrySet()){
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case RESP_DAILY_DATA_UPDATE:
		        short count = (short)dailyInfoMap.size();
		        buff.writeShort(count);
		        Iterator<Short> it = dailyInfoMap.keySet().iterator();
		        while (it.hasNext()) {
			        Short short1 = (Short) it.next();
			        buff.writeShort(short1);
			        buff.writeInt(dailyInfoMap.get(short1));
		        }
            default:
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    public byte getRespType() {
        return respType;
    }

    public void setRespType(byte respType) {
        this.respType = respType;
    }

    public List<RoleTagFightDelta> getRoleTagFightDeltaList() {
        return roleTagFightDeltaList;
    }

    public void setRoleTagFightDeltaList(List<RoleTagFightDelta> roleTagFightDeltaList) {
        this.roleTagFightDeltaList = roleTagFightDeltaList;
    }

    public byte getIsSuperAwardToday() {
        return isSuperAwardToday;
    }

    public void setIsSuperAwardToday(byte isSuperAwardToday) {
        this.isSuperAwardToday = isSuperAwardToday;
    }

    public List<RoleTmpDayInfo> getRoleTmpDayInfoList() {
        return roleTmpDayInfoList;
    }

    public void setRoleTmpDayInfoList(List<RoleTmpDayInfo> roleTmpDayInfoList) {
        this.roleTmpDayInfoList = roleTmpDayInfoList;
    }


    public DailyBallStageVo getDailyBallStageVo() {
        return dailyBallStageVo;
    }

    public void setDailyBallStageVo(DailyBallStageVo dailyBallStageVo) {
        this.dailyBallStageVo = dailyBallStageVo;
    }

    public short getDailyId() {
        return dailyId;
    }

    public void setDailyId(short dailyId) {
        this.dailyId = dailyId;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public int getRoleOwnDailyBallScore() {
        return roleOwnDailyBallScore;
    }

    public void setRoleOwnDailyBallScore(int roleOwnDailyBallScore) {
        this.roleOwnDailyBallScore = roleOwnDailyBallScore;
    }

    public byte getChooseTagId() {
        return chooseTagId;
    }

    public void setChooseTagId(byte chooseTagId) {
        this.chooseTagId = chooseTagId;
    }

    public byte getAwardType() {
        return awardType;
    }

    public byte getShowAwardType() {
        return showAwardType;
    }

    public void setShowAwardType(byte showAwardType) {
        this.showAwardType = showAwardType;
    }

    public void setAwardType(byte awardType) {
        this.awardType = awardType;
    }

    public Map<Short, Integer> getDailyInfoMap() {
        return dailyInfoMap;
    }

    public void setDailyInfoMap(Map<Short, Integer> dailyInfoMap) {
        this.dailyInfoMap = dailyInfoMap;
    }
}
