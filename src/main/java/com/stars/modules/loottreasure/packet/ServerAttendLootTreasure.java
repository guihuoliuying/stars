package com.stars.modules.loottreasure.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.loottreasure.LootTreasureModule;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * 客户端请求进入野外夺宝;
 * Created by panzhenfeng on 2016/10/10.
 */
public class ServerAttendLootTreasure  extends PlayerPacket {

    public ServerAttendLootTreasure(){

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.S_ATTEND_LOOTTREASURE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
    }


    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", LootTreasurePacketSet.S_ATTEND_LOOTTREASURE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        LootTreasureModule lootTreasureModule = module(MConst.LootTreasure);
        lootTreasureModule.requestAttend();
    }
}
