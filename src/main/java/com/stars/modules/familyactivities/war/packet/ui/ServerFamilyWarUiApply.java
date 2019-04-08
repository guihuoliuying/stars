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
import com.stars.services.family.FamilyAuth;
import com.stars.util.LogUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class ServerFamilyWarUiApply extends PlayerPacket {

    public static final byte SUBTYPE_INFO = 0x00; // 请求报名/参战名单
    public static final byte SUBTYPE_APPLY = 0x01; // 报名
    public static final byte SUBTYPE_APPLY_CANCEL = 0x02; // 取消报名
    public static final byte SUBTYPE_CONFIRM = 0x03; // 确认名单

    public byte subtype;
    public Set<Long> teamSheet;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_UI_APPLY));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule familyModule = module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        long familyId = auth.getFamilyId();
        long roleId = getRoleId();
        if (familyId <= 0){
            PacketManager.send(roleId,new ClientText("未加入任何家族"));
            return;
        }
        LogUtil.info("familywar|发包阶段==家族:{}的玩家:{} 报名 战斗类型:{}", familyId, roleId, FamilyWarConst.battleType);
        switch (subtype) {
            case SUBTYPE_INFO:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().sendApplicationSheet(familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().sendApplicationSheet(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().sendApplicationSheet(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
            case SUBTYPE_APPLY:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().apply(familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().apply(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().apply(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
            case SUBTYPE_APPLY_CANCEL:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().cancelApply(familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().cancelApply(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().cancelApply(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId);
                }
                break;
            case SUBTYPE_CONFIRM:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().confirmTeamSheet(familyId, roleId, teamSheet);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().confirmTeamSheet(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId, teamSheet);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().confirmTeamSheet(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyId, roleId, teamSheet);
                }
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_APPLY;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        if (subtype == SUBTYPE_CONFIRM) {
            teamSheet = new HashSet<>();
            int size = buff.readInt();
            for (int i = 0; i < size; i++) {
                teamSheet.add(Long.parseLong(buff.readString())); // 参赛名单（roleId）
            }
        }
    }
}
