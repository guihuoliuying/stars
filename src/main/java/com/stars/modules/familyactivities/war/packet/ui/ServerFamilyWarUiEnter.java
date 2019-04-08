package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class ServerFamilyWarUiEnter extends PlayerPacket {

    public static final byte SUBTYPE_ENTER = 0x00; // 进入
    public static final byte SUBTYPE_CANCEL = 0x01; // 取消

    private byte subtype;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_UI_ENTER));
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
            case SUBTYPE_ENTER:
                LogUtil.info("发包阶段==家族:{}的玩家:{}进入精英战场|赛事类型:{}", familyId, roleId, FamilyWarConst.battleType);
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().enterFight(familyId, roleId, FighterCreator.createPlayer(moduleMap()));
                }
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().enterFight(FamilyWarUtil.getFamilyWarServerId(),
                            MultiServerHelper.getServerId(), familyId, roleId, FighterCreator.createPlayer(moduleMap()));
                }
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().enterFight(FamilyWarUtil.getFamilyWarServerId(),
                            MultiServerHelper.getServerId(), familyId, roleId, FighterCreator.createPlayer(moduleMap()));
                }
                break;
            case SUBTYPE_CANCEL:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().cancelFight(familyId, roleId);
                }
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().cancelFight(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(),
                            familyId, roleId);
                }
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().cancelFight(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
        }

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_ENTER;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
}
