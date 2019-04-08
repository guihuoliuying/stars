package com.stars.modules.newofflinepvp.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.newofflinepvp.NewOfflinePvpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-03-09 19:33
 */
public class ServerNewOfflinePvp extends PlayerPacket {
    private static final byte view = 0x00;//打开页面
    private static final byte rankAward = 0x01;//排行榜
    private static final byte battleReport = 0x02;//战报
    private static final byte fight = 0x03;//战斗
    private static final byte buyCount = 0x04;//购买次数
    private static final byte fightCount = 0x06;//战斗次数

    private byte subtype;
    private String fightId;
    private byte roleOrRobot;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case fight:
                fightId = buff.readString();
                roleOrRobot = buff.readByte();
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", NewOfflinePvpPacketSet.S_OFFLINEPVP));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.OfflinePvp)) {
            PacketManager.send(player.id(), new ClientText("竞技场未开放"));
            return;
        }
        NewOfflinePvpModule module = module(MConst.NewOfflinePvp);
        switch (subtype) {
            case view:
                module.view();
                break;
            case rankAward:
                module.sendRankList();
                break;
            case battleReport:
                module.sendBattleReport();
                break;
            case fight:
                module.enterFight(Long.parseLong(fightId), roleOrRobot);
                break;
            case buyCount:
                module.buyCount();
                break;
            case fightCount:
                module.getFightCount();
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return NewOfflinePvpPacketSet.S_OFFLINEPVP;
    }
}
