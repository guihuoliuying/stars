package com.stars.modules.soul.packet;

import com.stars.core.attr.Attribute;
import com.stars.modules.soul.SoulManager;
import com.stars.modules.soul.SoulPacketSet;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.usrdata.RoleSoul;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Collection;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class ClientSoulPacket extends Packet {
    private short subType;
    public static final short SEND_MAIN_UI = 1;//下发主界面信息
    public static final short SEND_UPGRADE = 2;//下发升级成功通知
    public static final short SEND_ONEKEY_UPGRADE = 3;//下发一键升级结果
    public static final short SEND_BREAK_SUCCESS = 4;//下发突破结果


    private RoleSoul roleSoul;
    private Collection<SoulLevel> soulLevels;//各个位置等级
    private int totalFightScore;//总战力
    private Attribute totalAttribute;//总属性
    private Attribute deltaAttribute;//相差属性
    private String costItem;//下一步操作消耗道具

    public ClientSoulPacket() {
    }

    public ClientSoulPacket(short subType) {
        this.subType = subType;
    }

    @Override
    public short getType() {
        return SoulPacketSet.C_SOUL;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MAIN_UI: {
                buff.writeInt(totalFightScore);//元神总战力
                buff.writeString(totalAttribute.getAttributeStr());//获取属性字符串
                buff.writeString(deltaAttribute.getAttributeStr());//相差属性字符串
                buff.writeString(costItem);//下一步操作消耗材料
                buff.writeInt(roleSoul.getStage());//当前阶段
                SoulLevel nextSoulLevel = roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage());
                buff.writeInt(nextSoulLevel==null?1:nextSoulLevel.getSoulGodType());//当前位置
                buff.writeInt(roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage()) == null ? 1 : 0);//是否需要突破：1需要，0不需要
                buff.writeInt(roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage()) == null && roleSoul.getStage() == SoulManager.maxSoulStage ? 1 : 0);//是否满级
                buff.writeInt(soulLevels.size());
                for (SoulLevel soulLevel : soulLevels) {
                    buff.writeInt(soulLevel.getSoulGodType());//位置
                    buff.writeInt(soulLevel.getSoulGodLevel());//当前等级
                }
            }
            break;
            case SEND_UPGRADE: {

            }
            break;
            case SEND_ONEKEY_UPGRADE: {

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

    public void setRoleSoul(RoleSoul roleSoul) {
        this.roleSoul = roleSoul;
    }

    public void setSoulLevels(Collection<SoulLevel> soulLevels) {
        this.soulLevels = soulLevels;
    }

    public void setTotalFightScore(int totalFightScore) {
        this.totalFightScore = totalFightScore;
    }

    public void setTotalAttribute(Attribute totalAttribute) {
        this.totalAttribute = totalAttribute;
    }

    public Attribute getDeltaAttribute() {
        return deltaAttribute;
    }

    public void setDeltaAttribute(Attribute deltaAttribute) {
        this.deltaAttribute = deltaAttribute;
    }

    public void setCostItem(String costItem) {
        this.costItem = costItem;
    }
}
