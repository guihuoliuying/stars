package com.stars.modules.newservermoney.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.newservermoney.NewServerMoneyPacketSet;
import com.stars.modules.newservermoney.prodata.NewServerMoneyVo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public class ClientNewServerMoney extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_DATA = 1;// 下发数据
    public static final byte MY_REWARD_RECORD = 2;// 我的获奖记录
    public static final byte REWARD_RESULT = 3;// 发奖结果

    /* 参数 */
    private long startTimeStamp = 0;//活动开始时间戳
	private long endTimeStamp = 0;//活动结束时间戳
    private Map<Integer, NewServerMoneyVo> nsMoneyVoMap;// 撒钱产品数据
    private int curActId;// 当前活动Id
    private String myRewardRecord;// 我的获奖记录
    private int moneyRewardType;// 发奖Id
    private List<Summary> rewardResult;// 发奖结果

    public ClientNewServerMoney() {
    }

    public ClientNewServerMoney(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewServerMoneyPacketSet.C_NSMONEY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_DATA:
                writeMoneyVo(buff);
                break;
            case MY_REWARD_RECORD:
                buff.writeString(myRewardRecord);// 格式:时间戳+奖励type,时间戳+奖励type
                break;
            case REWARD_RESULT:
                writeRewardResult(buff);
                break;
        }
    }

    private void writeMoneyVo(NewByteBuffer buff) {
    	//活动开始时间
        buff.writeString(Long.toString(startTimeStamp));
    	//活动结束时间
        buff.writeString(Long.toString(endTimeStamp));
    	
        byte size = (byte) (nsMoneyVoMap == null ? 0 : nsMoneyVoMap.size());
        buff.writeByte(size);
        if (size == 0) {
            return;
        }
        buff.writeInt(curActId);
        for (NewServerMoneyVo vo : nsMoneyVoMap.values()) {
            vo.writeToBuff(buff);
        }
    }

    private void writeRewardResult(NewByteBuffer buff) {
        buff.writeInt(moneyRewardType);
        byte size = (byte) (rewardResult == null ? 0 : rewardResult.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Summary summary : rewardResult) {
            RoleSummaryComponent rsc = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            buff.writeString(String.valueOf(summary.getRoleId()));// roleid
            buff.writeString(rsc.getRoleName());// 名字
            buff.writeInt(rsc.getRoleJob());// 职业
            buff.writeInt(rsc.getRoleLevel());// 等级
            FamilySummaryComponent fsc = (FamilySummaryComponent) summary.getComponent(SummaryConst.C_FAMILY);
            buff.writeString(fsc.getFamilyName());// 家族名称
        }
    }

    public void setStartTimeStamp(long value){
    	this.startTimeStamp = value;
    }
    
    public void setEndTimeStamp(long value){
    	this.endTimeStamp = value;
    }
    
    public void setNsMoneyVoMap(int curActId, Map<Integer, NewServerMoneyVo> nsMoneyVoMap) {
        this.curActId = curActId;
        this.nsMoneyVoMap = nsMoneyVoMap;
    }

    public void setMyRewardRecord(String myRewardRecord) {
        this.myRewardRecord = myRewardRecord;
    }

    public void setMoneyRewardType(int moneyRewardType) {
        this.moneyRewardType = moneyRewardType;
    }

    public void setRewardResult(List<Summary> rewardResult) {
        this.rewardResult = rewardResult;
    }
}
