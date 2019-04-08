package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
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
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-04-13 9:36
 */
public class ServerFamilyWarSafeSceneEnter extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_ENTER_SAFE_SCENE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        long roleId = getRoleId();
        if (familyId <= 0){
            PacketManager.send(roleId,new ClientText("未加入任何家族"));
            return;
        }
        LogUtil.info("familywar|发包阶段==家族:{}的玩家:{}进入家族战备战场景 战斗类型:{}", familyId, roleId, FamilyWarConst.battleType);
        if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
            ServiceHelper.familyWarLocalService().enterSafeScene(familyId, roleId);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().enterSafeScene(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().enterSafeScene(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
        }
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_ENTER_SAFE_SCENE;
    }
}
