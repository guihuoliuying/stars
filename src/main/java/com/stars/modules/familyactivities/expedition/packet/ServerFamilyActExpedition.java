package com.stars.modules.familyactivities.expedition.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionModule;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;


/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class ServerFamilyActExpedition extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00; // 打开界面
    public static final byte SUBTYPE_FIGHT = 0x10; // 进入战斗
    public static final byte SUBTYPE_AWARD = 0x20;
    public static final byte SUBTYPE_BUFF = 0x30; // 加BUFF

    private byte subtype;
    private int expeditionId;
    private int itemId;
    private int buffId;

    @Override
    public short getType() {
        return FamilyActExpeditionPacketSet.S_EXPEDITION;
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActExpeditionPacketSet.S_EXPEDITION));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyActExpeditionModule module = (FamilyActExpeditionModule) module(MConst.FamilyActExpe);
        switch (subtype) {
            case SUBTYPE_VIEW:
                module.view();
                break;
            case SUBTYPE_FIGHT:
                module.fight(expeditionId);
                break;
            case SUBTYPE_AWARD:
                module.getAward(itemId);
                break;
            case SUBTYPE_BUFF:
                module.addBuff(buffId);
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_FIGHT:
                expeditionId = buff.readInt();
                break;
            case SUBTYPE_AWARD:
                itemId = buff.readInt();
                break;
            case SUBTYPE_BUFF:
                buffId = buff.readInt();
                break;
        }
    }
}
