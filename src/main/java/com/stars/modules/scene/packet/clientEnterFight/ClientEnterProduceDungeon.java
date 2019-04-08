package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by liuyuheng on 2016/9/1.
 */
public class ClientEnterProduceDungeon extends ClientEnterDungeon {
    private int limitTime;// 倒计时,单位秒
    private List<String> waveMonsterTypeList;// 波数怪物类型的信息列表;
    private int showProgressBar=1;//是否显示进度条

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        super.writeToBuffer(buff);
        buff.writeInt(limitTime);
        writeMonsterTypeCountInfo(buff);
        buff.writeInt(showProgressBar);//显示进度条，1：显示，0：不显示
    }

    private void writeMonsterTypeCountInfo(NewByteBuffer buff) {
        short size = (short) (waveMonsterTypeList == null ? 0 : waveMonsterTypeList.size());
        buff.writeShort(size);
        if (waveMonsterTypeList != null) {
            for (int i = 0, len = waveMonsterTypeList.size(); i < len; i++) {
                buff.writeString(waveMonsterTypeList.get(i));
            }
        }
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public void setWaveMonsterTypeList(List<String> waveMonsterTypeList) {
        this.waveMonsterTypeList = waveMonsterTypeList;
    }

    public int getShowProgressBar() {
        return showProgressBar;
    }

    public void setShowProgressBar(int showProgressBar) {
        this.showProgressBar = showProgressBar;
    }
}
