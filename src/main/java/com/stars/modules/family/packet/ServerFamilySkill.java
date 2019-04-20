package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/9/8.
 */
public class ServerFamilySkill extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00; // 查看家族心法
    public static final byte SUBTYPE_UPGRADE = 0x10; // 升级家族心法
    public static final byte SUBTYPE_UPGRADE_AMAP = 0x11; // 尽可能多升级家族心法

    private byte subtype;
    public String attribute;
    public int nextLevel;

    @Override
    public void execPacket(Player player) {
        FamilyModule familyModule = (FamilyModule) module(MConst.Family);
        switch (subtype) {
            case SUBTYPE_VIEW:
                ClientFamilySkill packet = new ClientFamilySkill(ClientFamilySkill.SUBTYPE_VIEW);
                packet.setTotalSkillLevelMap(familyModule.getRoleFamilyPo().getSkillLevelMap());
                PlayerUtil.send(getRoleId(), packet);
                break;
            case SUBTYPE_UPGRADE:
                familyModule.upgradeSkillLevel(attribute, nextLevel);
                break;
            case SUBTYPE_UPGRADE_AMAP:
                familyModule.upgradeSkillLevelAmap();
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_SKILL;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_UPGRADE:
                attribute = buff.readString();
                nextLevel = buff.readInt();
                break;
        }
    }
}
