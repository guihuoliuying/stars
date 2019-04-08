package com.stars.modules.family.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.util.LogUtil;

import java.util.Map;

import static com.stars.modules.family.FamilyManager.rpGiverRewardMap;
import static com.stars.modules.family.FamilyManager.rpSeizerAwardDropId;

/**
 * Created by zhaowenshuo on 2016/9/7.
 */
public class ServerFamilyRedPacket extends PlayerPacket {

    public static final byte SUBTYPE_ALL_INFO = 0x00; // 红包界面的信息
    public static final byte SUBTYPE_SELF_INFO = 0x01; // 自身信息
    public static final byte SUBTYPE_NOTIFY = 0x0F; // 通知有红包可抢

    public static final byte SUBTYPE_GIVE = 0x10; // 派发红包
    public static final byte SUBTYPE_SEIZE = 0x11; // 抢夺红包

    private byte subtype;
    private long redPacketId;

    public ServerFamilyRedPacket() {
    }

    public ServerFamilyRedPacket(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyPacketSet.S_RED_PACKET));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule familyModule = (FamilyModule) module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        if (auth == null) {
            familyModule.warn("数据加载中");
            return;
        }
        DropModule dropModule = (DropModule) module(MConst.Drop);
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        switch (subtype) {
            case SUBTYPE_ALL_INFO:
                ServiceHelper.familyRedPacketService().sendRedPacketInfo(familyModule.getAuth());
                break;
            case SUBTYPE_GIVE:
                if (ServiceHelper.familyRedPacketService().giveRedPacket(auth)) {
                    toolModule.addAndSend(rpGiverRewardMap, EventType.FAIMLYRED.getCode());
                }
                break;
            case SUBTYPE_SEIZE:
                if (ServiceHelper.familyRedPacketService().seizeRedPacket(auth, redPacketId)) {
                    Map<Integer, Integer> toolMap = dropModule.executeDrop(rpSeizerAwardDropId, 1, true);
                    toolModule.addAndSend(toolMap, EventType.FAIMLYRED.getCode());
                    ServiceHelper.familyRedPacketService().updateSeizedRedPacketInfo(auth, redPacketId, toolMap);
                    // todo: record it here!

                }
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_RED_PACKET;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_SEIZE:
                this.redPacketId = Long.parseLong(buff.readString());
                break;
        }
    }
}
