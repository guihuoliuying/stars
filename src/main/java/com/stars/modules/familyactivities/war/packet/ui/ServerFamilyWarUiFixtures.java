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
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.FamilyData;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/12/14.
 */
public class ServerFamilyWarUiFixtures extends PlayerPacket {

    public static final byte SUBTYPE_ALL = 0x00;
    public static final byte SUBTYPE_UPDATED = 0x01;

    private byte subtype;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_UI_FIXTURES));
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
        switch (subtype) {
            case SUBTYPE_ALL:
                LogUtil.info("familywar|发包阶段==家族:{}的玩家:{} 发送赛程表 战斗类型:{}", familyId, roleId, FamilyWarConst.battleType);
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().sendFixtures(familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    FamilyData data = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
                    MainRpcHelper.familyWarQualifyingService().sendFixtures(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId, data.getFamilyPo().getTotalFightScore());
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().sendFixtures(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
            case SUBTYPE_UPDATED:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().sendUpdatedFixtures(familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().sendUpdatedFixtures(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().sendUpdatedFixtures(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_FIXTURES;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
}
