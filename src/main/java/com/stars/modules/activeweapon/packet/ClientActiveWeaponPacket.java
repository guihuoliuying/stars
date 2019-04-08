package com.stars.modules.activeweapon.packet;

import com.stars.modules.activeweapon.ActiveWeaponManager;
import com.stars.modules.activeweapon.ActiveWeaponPacketSet;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ClientActiveWeaponPacket extends Packet {
    private short subType;
    public static final short SEND_ACTIVE_WEAPON_LIST = 1;//下发活跃神兵产品数据
    public static final short SEND_TAKE_REWARD_RECORD = 2;//下发个人领奖信息记录
    private Map<Integer, Byte> rewardRecordMap;

    @Override
    public short getType() {
        return ActiveWeaponPacketSet.C_ACTIVE_WEAPON;
    }

    public ClientActiveWeaponPacket(short subType) {
        this.subType = subType;
    }

    public ClientActiveWeaponPacket() {
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_ACTIVE_WEAPON_LIST: {
                writeActiveWeapons(buff);
            }
            break;
            case SEND_TAKE_REWARD_RECORD: {
                writeRewardRecord(buff);
            }
            break;
        }
    }

    private void writeRewardRecord(NewByteBuffer buff) {
        buff.writeInt(rewardRecordMap.size());
        for (Map.Entry<Integer, Byte> entry : rewardRecordMap.entrySet()) {
            buff.writeInt(entry.getKey());
            buff.writeByte(entry.getValue());
        }
    }

    /**
     * 下发产品表
     *
     * @param buff
     */
    private void writeActiveWeapons(NewByteBuffer buff) {
        buff.writeInt(ActiveWeaponManager.activeWeaponVoMap.size());
        for (Map.Entry<Integer, ActiveWeaponVo> entry : ActiveWeaponManager.activeWeaponVoMap.entrySet()) {
            ActiveWeaponVo activeWeaponVo = entry.getValue();
            buff.writeInt(activeWeaponVo.getId());
            buff.writeInt(activeWeaponVo.getType());
            buff.writeString(activeWeaponVo.getCondition());
            buff.writeInt(activeWeaponVo.getReward());
            buff.writeString(activeWeaponVo.getShowid());
            buff.writeString(activeWeaponVo.getDesc());
            buff.writeByte(rewardRecordMap.get(entry.getKey()));
            buff.writeInt(activeWeaponVo.getItemsign());
        }
        buff.writeString(ActiveWeaponManager.activeweaponItemshow);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public Map<Integer, Byte> getRewardRecordMap() {
        return rewardRecordMap;
    }

    public void setRewardRecordMap(Map<Integer, Byte> rewardRecordMap) {
        this.rewardRecordMap = rewardRecordMap;
    }
}
