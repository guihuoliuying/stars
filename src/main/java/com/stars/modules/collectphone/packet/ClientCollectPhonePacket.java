package com.stars.modules.collectphone.packet;

import com.stars.modules.collectphone.CollectPhoneManager;
import com.stars.modules.collectphone.CollectPhonePacketSet;
import com.stars.modules.collectphone.prodata.StepOperateAct;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by huwenjun on 2017/9/13.
 */
public class ClientCollectPhonePacket extends Packet {
    private short subType;
    private int actType;
    private String data;
    private int step;
    private int remainTimes;//剩余时间
    private int joinStatus;//领奖状态
    public static final short SEND_MAIN_UI_DATA = 1;//发送主界面数据
    public static final short SEND_SUBMIT_QUESTION = 2;//发送提交数据响应，每一步结束下发
    public static final short SEND_SUBMIT_PHONE = 3;//发送提交手机号码响应
    public static final short SEND_SUBMIT_VERIFY_CODE = 4;//发送提交验证码响应
    public static final short SEND_FINISH = 5;//奖励发放完毕

    public ClientCollectPhonePacket() {
    }

    public ClientCollectPhonePacket(int actType, short subType) {
        this.actType = actType;
        this.subType = subType;
    }

    @Override
    public short getType() {
        return CollectPhonePacketSet.C_COLLECT_PHONE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(actType);
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MAIN_UI_DATA: {
                buff.writeInt(step);//当前步数
                buff.writeInt(joinStatus);//0表示未领奖，1表示已领奖
                buff.writeInt(remainTimes);//剩余秒数，负数表示已冷却好
                buff.writeInt(CollectPhoneManager.stepOperateActList.size());
                for (StepOperateAct stepOperateAct : CollectPhoneManager.stepOperateActList) {
                    stepOperateAct.writeBuff(buff);
                }
            }
            break;
            case SEND_SUBMIT_PHONE: {
                buff.writeString(data);
            }
            break;
            case SEND_SUBMIT_VERIFY_CODE: {
                buff.writeString(data);
            }
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getRemainTimes() {
        return remainTimes;
    }

    public void setRemainTimes(int remainTimes) {
        this.remainTimes = remainTimes;
    }

    public int getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(int joinStatus) {
        this.joinStatus = joinStatus;
    }
}
