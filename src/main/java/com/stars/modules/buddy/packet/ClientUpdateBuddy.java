package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ClientUpdateBuddy extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte CHANGE_FOLLOW_BUDDY = 1;// 改变跟随伙伴
    public static final byte CHANGE_FIGHT_BUDDY = 2;// 改变出战伙伴
    public static final byte ADDEXP = 3;// 加经验
    public static final byte LEVELUP = 4;// 升级
    public static final byte UPGRADE_STAGE_LV = 5;// 提升阶级
    public static final byte PUTON_EQUIP = 6;// 穿上装备
    public static final byte UPGRADE_ARM_LV = 7;// 提升武装等级
    public static final byte ACTIVE_BUDDY = 8;// 激活伙伴

    /* 参数 */
    private List<RoleBuddy> list;

    public ClientUpdateBuddy() {
    }

    public ClientUpdateBuddy(byte sendType, List<RoleBuddy> list) {
        this.sendType = sendType;
        this.list = list;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BuddyPacketSet.C_UPDATE_BUDDY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        byte size = (byte) (list == null ? 0 : list.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        switch (sendType) {
            case CHANGE_FOLLOW_BUDDY:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeByte(roleBuddy.getIsFollow());
                }
                break;
            case CHANGE_FIGHT_BUDDY:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeByte(roleBuddy.getIsFight());
                }
                break;
            case ADDEXP:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeInt(roleBuddy.getExp());
                }
                break;
            case LEVELUP:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeInt(roleBuddy.getLevel());
                    buff.writeInt(roleBuddy.getExp());
                    buff.writeInt(roleBuddy.getFightScore());
                    roleBuddy.getAttribute().writeToBuffer(buff);// 下发属性
                }
                break;
            case UPGRADE_STAGE_LV:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeInt(roleBuddy.getStageLevel());
                    buff.writeInt(roleBuddy.getFightScore());
                    buff.writeInt(roleBuddy.getNextStageLvFS());// 下一阶战力
                    roleBuddy.getAttribute().writeToBuffer(buff);// 下发属性
                }
                break;
            case PUTON_EQUIP:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    roleBuddy.writeEquipStatus(buff);
                    buff.writeInt(roleBuddy.getFightScore());
                    roleBuddy.getAttribute().writeToBuffer(buff);// 下发属性
                }
                break;
            case UPGRADE_ARM_LV:
                for (RoleBuddy roleBuddy : list) {
                    buff.writeInt(roleBuddy.getBuddyId());
                    buff.writeInt(roleBuddy.getArmLevel());
                    roleBuddy.writeEquipStatus(buff);
                    buff.writeInt(roleBuddy.getFightScore());
                    roleBuddy.getAttribute().writeToBuffer(buff);// 下发属性
                }
                break;
            case ACTIVE_BUDDY:
                for (RoleBuddy roleBuddy : list) {
                    roleBuddy.writeToBuff(buff);
                }
                break;
        }
    }
}
