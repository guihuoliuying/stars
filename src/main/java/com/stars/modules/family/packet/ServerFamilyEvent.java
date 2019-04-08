package com.stars.modules.family.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class ServerFamilyEvent extends PlayerPacket {

    public static final byte SUBTYPE_DONATE = 0x00;
    public static final byte SUBTYPE_EVENT = 0x01;

    private byte subtype;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyPacketSet.S_EVENT));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule familyModule = (FamilyModule) module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        if (auth == null) {
            familyModule.warn("数据加载中");
        }
        if (auth.getFamilyId() <= 0) {
            familyModule.warn("没有权限");
        }
        ServiceHelper.familyEventService().sendEvent(auth, ServerFamilyEvent.SUBTYPE_EVENT);
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_EVENT;
    }
}
