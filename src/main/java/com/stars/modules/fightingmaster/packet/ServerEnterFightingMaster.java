package com.stars.modules.fightingmaster.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.fightingmaster.FightingMasterModule;
import com.stars.modules.fightingmaster.FightingMasterPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/11/16.
 */
public class ServerEnterFightingMaster extends PlayerPacket {

    public static final byte view = 0x00;
    public static final byte fightCount = 0x09;
    private byte subType;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FightingMasterPacketSet.S_ENTER_FIGHTINGMASTER));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FightingMasterModule fm = module(MConst.FightingMaster);
        if (subType == view) {
            fm.enterFightingMaster();
        } else if (subType == fightCount) {
            fm.getFightCount();
        }
    }

    @Override
    public short getType() {
        return FightingMasterPacketSet.S_ENTER_FIGHTINGMASTER;
    }
}
