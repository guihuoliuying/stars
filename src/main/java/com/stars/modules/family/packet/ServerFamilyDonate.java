package com.stars.modules.family.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/9/5.
 */
public class ServerFamilyDonate extends PlayerPacket {

    public static final byte SUBTYPE_INFO = 0x00;
    public static final byte SUBTYPE_DONATE = 0x01;
    public static final byte SUBTYPE_DNOATE_RMB = 0x02;

    private byte subtype;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyPacketSet.S_DONATE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule familyModule = (FamilyModule) module(MConst.Family);
        switch (subtype) {
            case SUBTYPE_INFO:
                familyModule.sendDonateInfo();
                ServiceHelper.familyEventService().sendEvent(familyModule.getAuth(), ServerFamilyEvent.SUBTYPE_DONATE);
                break;
            case SUBTYPE_DONATE:
                familyModule.donate();
                break;
            case SUBTYPE_DNOATE_RMB:
                familyModule.donateRmb();
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_DONATE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
}
