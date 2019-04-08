package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class ClientEnterOfflinePvp extends ClientEnterFight {
    private int limitTime;// 倒计时,单位秒
    private Map<Integer, Integer> growBuff;// 成长buff
    private byte autoFlag = 0;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(limitTime);
        buff.writeByte(this.autoFlag);
        byte size = (byte) (growBuff == null ? 0 : growBuff.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Integer, Integer> entry : growBuff.entrySet()) {
            buff.writeInt(entry.getKey());// buffId
            buff.writeInt(entry.getValue());// buffLevel
        }
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public void setGrowBuff(Map<Integer, Integer> growBuff) {
        this.growBuff = growBuff;
        addBuffData(growBuff);
    }

    public byte getAutoFlag() {
        return autoFlag;
    }

    public void setAutoFlag(byte autoFlag) {
        this.autoFlag = autoFlag;
    }
}
