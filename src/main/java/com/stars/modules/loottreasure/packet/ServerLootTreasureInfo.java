package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.util.ServerLogConst;

/**
 * 客户端请求查看野外夺宝的信息;
 * Created by panzhenfeng on 2016/10/10.
 */
public class ServerLootTreasureInfo  extends PlayerPacket {

    @Override
    public short getType() {
        return LootTreasurePacketSet.S_LOOTTREASURE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
    }


    @Override
    public void execPacket(Player player) {
        RoleModule roleModule = (RoleModule)module(MConst.Role);
        ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_INFO);
        clientLootTreasureInfo.setStartStamp(ServiceHelper.lootTreasureService().getStartActivityTimeStamp());
        clientLootTreasureInfo.setEndStamp(ServiceHelper.lootTreasureService().getEndActivityTimeStamp());
        clientLootTreasureInfo.referenceLevel = roleModule.getLevel();
        roleModule.send(clientLootTreasureInfo);
        ServerLogModule log = (ServerLogModule)module(MConst.ServerLog);
        log.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_101.getThemeId(),0);
    }
}