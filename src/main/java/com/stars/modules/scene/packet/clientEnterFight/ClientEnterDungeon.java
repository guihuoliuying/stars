package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Set;

/**
 * Created by liuyuheng on 2016/8/30.
 */
public class ClientEnterDungeon extends ClientEnterFight {

    private List<MonsterSpawnVo> areaSpawnList;// 预加载坐标刷怪配置
    private Set<Integer> itemIdSet;// 怪物掉落物品IdSet,预加载掉落模型
    private byte autoFlag = 0;
    private int failTime = 0;//副本失败时间

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeBase(buff);
        writeAreaSpawn(buff);
        writeDropItemId(buff);
        buff.writeByte(this.autoFlag);
        buff.writeInt(this.failTime);
    }

    private void writeAreaSpawn(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (areaSpawnList == null ? 0 : areaSpawnList.size());
        buff.writeShort(size);
        if (areaSpawnList != null) {
            for (MonsterSpawnVo monsterSpawnVo : areaSpawnList) {
                monsterSpawnVo.writeToBuff(buff);
            }
        }
    }

    private void writeDropItemId(NewByteBuffer buff) {
        byte size = (byte) (itemIdSet == null ? 0 : itemIdSet.size());
        buff.writeByte(size);
        if(itemIdSet != null){
            for (int itemId : itemIdSet) {
                buff.writeInt(itemId);
            }
        }
    }

    public void setAreaSpawnList(List<MonsterSpawnVo> areaSpawnList) {
        this.areaSpawnList = areaSpawnList;
    }

    public void setItemIdSet(Set<Integer> itemIdSet) {
        this.itemIdSet = itemIdSet;
    }

    public void setAutoFlag(byte autoFlag) {
        this.autoFlag = autoFlag;
    }

    public void setFailTime(int failTime) {
        this.failTime = failTime;
    }
}
