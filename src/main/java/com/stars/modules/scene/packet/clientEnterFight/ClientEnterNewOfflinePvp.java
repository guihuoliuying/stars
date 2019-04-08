package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-03-11 16:26
 */
public class ClientEnterNewOfflinePvp extends ClientEnterFight {
    private int limitTime;// 倒计时,单位秒
    private byte autoFlag = 0;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(limitTime);
        buff.writeByte(this.autoFlag);
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public void setAutoFlag(byte autoFlag) {
        this.autoFlag = autoFlag;
    }
}
