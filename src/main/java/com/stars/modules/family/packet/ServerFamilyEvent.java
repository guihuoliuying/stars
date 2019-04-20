package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class ServerFamilyEvent extends PlayerPacket {

    public static final byte SUBTYPE_DONATE = 0x00;
    public static final byte SUBTYPE_EVENT = 0x01;

    private byte subtype;

    @Override
    public void execPacket(Player player) {
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
