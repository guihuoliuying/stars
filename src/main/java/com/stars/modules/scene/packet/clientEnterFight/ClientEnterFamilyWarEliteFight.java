package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/12/6.
 */
public class ClientEnterFamilyWarEliteFight extends ClientEnterFight {

    private int limitTime;// 战场倒计时,单位秒
    private int startRemainderTime;//离战场开启的剩余时间

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(limitTime);
        buff.writeInt(startRemainderTime);
        LogUtil.info("limitTime:{},startRemainderTime:{}", limitTime, startRemainderTime);
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public int getStartRemainderTime() {
        return startRemainderTime;
    }

    public void setStartRemainderTime(int startRemainderTime) {
        this.startRemainderTime = startRemainderTime;
    }
}
