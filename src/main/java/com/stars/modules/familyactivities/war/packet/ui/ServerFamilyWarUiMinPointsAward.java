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
 * Created by zhaowenshuo on 2016/12/27.
 */
public class ServerFamilyWarUiMinPointsAward extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00; // 查看积分
    public static final byte SUBTYPE_ACQUIRE = 0x01; // 获得奖励

    private byte subtype;
    private long points;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyActWarPacketSet.S_FAMILY_WAR_UI_MIN_POINTS_AWARD));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        LogUtil.info("familywar|battyleType:{}", FamilyWarConst.battleType);
        switch (subtype) {
            case SUBTYPE_VIEW:
                LogUtil.info("familywar|发包阶段 玩家:{}查看积分 战斗类型:{}", getRoleId(), FamilyWarConst.battleType);
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().viewMinPointsAward(getRoleId());
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().viewMinPointsAward(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId());
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().viewMinPointsAward(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId());
                }
                break;
            case SUBTYPE_ACQUIRE:
                if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                    ServiceHelper.familyWarLocalService().acquireMinPointsAward(getRoleId(), points);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                    MainRpcHelper.familyWarQualifyingService().acquireMinPointsAward(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId(), points);
                } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                    MainRpcHelper.familyWarRemoteService().acquireMinPointsAward(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), getRoleId(), points);
                }
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_UI_MIN_POINTS_AWARD;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte(); // 0 - 查看，1 - 奖励
        switch (subtype) {
            case SUBTYPE_ACQUIRE:
                points = Long.parseLong(buff.readString());
                break;
        }
    }
}
