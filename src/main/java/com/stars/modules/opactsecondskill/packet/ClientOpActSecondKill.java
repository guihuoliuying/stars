package com.stars.modules.opactsecondskill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactsecondskill.OpActSecondKillPacketSet;
import com.stars.modules.opactsecondskill.prodata.SecKillVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class ClientOpActSecondKill extends PlayerPacket {
    public static final byte RESP_VIEW = 0x00; // 查看活动信息


    private byte subType;
    private SecKillVo secKillVo;
    private long countDownTime;
    private int currentPay;
    private int nextNeedPay;
    private byte canChange;
    private byte isClose;

    @Override
    public short getType() {
        return OpActSecondKillPacketSet.C_OpActSecondSkill;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType){
            case RESP_VIEW:
                buff.writeByte(isClose);
                if(isClose != (byte)1) {
                    buff.writeLong(countDownTime);
                    buff.writeInt(nextNeedPay);
                    buff.writeInt(currentPay);
                    buff.writeByte(canChange);
                    buff.writeInt(secKillVo.getId());
                    buff.writeInt(secKillVo.getRechargeValue());
                    buff.writeInt(secKillVo.getOldCost());
                    buff.writeInt(secKillVo.getNowCost());
                    buff.writeString(secKillVo.getDiscountIcon());
                    int size = secKillVo.getItemMap().size();
                    buff.writeInt(size);
                    for (Map.Entry<Integer, Integer> entry : secKillVo.getItemMap().entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        super.readFromBuffer(buff);
    }

    @Override
    public void execPacket(Player player) {

    }


    public byte getSubType() {
        return subType;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public SecKillVo getSecKillVo() {
        return secKillVo;
    }

    public void setSecKillVo(SecKillVo secKillVo) {
        this.secKillVo = secKillVo;
    }

    public long getCountDownTime() {
        return countDownTime;
    }

    public void setCountDownTime(long countDownTime) {
        this.countDownTime = countDownTime;
    }

    public int getCurrentPay() {
        return currentPay;
    }

    public void setCurrentPay(int currentPay) {
        this.currentPay = currentPay;
    }

    public int getNextNeedPay() {
        return nextNeedPay;
    }

    public void setNextNeedPay(int nextNeedPay) {
        this.nextNeedPay = nextNeedPay;
    }

    public byte getCanChange() {
        return canChange;
    }

    public void setCanChange(byte canChange) {
        this.canChange = canChange;
    }

    public byte getIsClose() {
        return isClose;
    }

    public void setIsClose(byte isClose) {
        this.isClose = isClose;
    }
}
