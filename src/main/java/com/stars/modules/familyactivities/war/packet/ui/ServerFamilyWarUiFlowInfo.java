package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.network.server.packet.PacketManager;

public class ServerFamilyWarUiFlowInfo extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId <= 0){
            PacketManager.send(player.id(),new ClientText("未加入任何家族"));
            return;
        }
        if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().containFamily(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, getRoleId());
            return;
        }
        send(new ClientFamilyWarUiFlowInfo());
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_MAIN;
    }

}
