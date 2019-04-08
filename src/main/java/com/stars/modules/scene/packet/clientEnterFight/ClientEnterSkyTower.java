package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by liuyuheng on 2016/8/31.
 */
public class ClientEnterSkyTower extends ClientEnterDungeon {
    private List<String> waveMonsterTypeList;// 波数怪物类型的信息列表;

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        super.writeToBuffer(buff);
        writeMonsterTypeCountInfo(buff);
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

    public void setWaveMonsterTypeList(List<String> waveMonsterTypeList) {
        this.waveMonsterTypeList = waveMonsterTypeList;
    }
}
