package com.stars.modules.name.packet;

import com.stars.modules.name.NamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/14.
 */
public class ClientRenamePacket extends Packet {
    private short subType;
    public static final short SEND_ROLE_RENAME_SUCCESS = 1;//角色改名成功通知
    public static final short SEND_RENAME_SURPLUS_TIMES = 2;//下发改名剩余次数
    public static final short SEND_FAMILY_RENAME_NOTIFY = 3;//通知家族族长改名通知
    public static final short SEND_RANDOM_NAME = 4;//发送随机名称
    public static final short SEND_FAMILY_RENAME_SUCCESS = 5;//家族改名成功通知
    private int surplusTimes;
    private Map<Integer, Integer> costItemMap;
    private String randomName;

    @Override
    public short getType() {
        return NamePacketSet.C_RENAME;
    }

    public ClientRenamePacket() {
    }

    public ClientRenamePacket(short subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_RENAME_SURPLUS_TIMES: {
                buff.writeInt(surplusTimes);//剩余次数
                buff.writeInt(costItemMap.size());//消耗品《itemid，count》
                for (Map.Entry<Integer, Integer> entry : costItemMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
            break;
            case SEND_RANDOM_NAME: {
                buff.writeString(randomName);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public int getSurplusTimes() {
        return surplusTimes;
    }

    public void setSurplusTimes(int surplusTimes) {
        this.surplusTimes = surplusTimes;
    }

    public Map<Integer, Integer> getCostItemMap() {
        return costItemMap;
    }

    public void setCostItemMap(Map<Integer, Integer> costItemMap) {
        this.costItemMap = costItemMap;
    }

    public String getRandomName() {
        return randomName;
    }

    public void setRandomName(String randomName) {
        this.randomName = randomName;
    }
}
