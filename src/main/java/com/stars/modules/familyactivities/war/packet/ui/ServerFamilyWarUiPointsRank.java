package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/12/19.
 */
public class ServerFamilyWarUiPointsRank extends PlayerPacket {

    public static final byte SUBTYPE_ELITE_FIGHT = 0; // 精英战
    public static final byte SUBTYPE_NORMAL_FIGHT = 1; // 匹配战

    private byte subtype;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_UI_POINTS_RANK));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        LogUtil.info("familywar|发包阶段 玩家:{} 查看积分排行榜 战斗类型:{}", getRoleId(), FamilyWarConst.battleType);
        if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
            ServiceHelper.familyWarLocalService().sendPointsRank(getRoleId(), subtype);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().sendPointsRank(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId(), subtype);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().sendPointsRank(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId(), subtype);
        }
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_POINTS_RANK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
}
