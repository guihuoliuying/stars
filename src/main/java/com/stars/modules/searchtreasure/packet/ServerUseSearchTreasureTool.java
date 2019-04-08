package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求使用探宝地图里的道具;
 * Created by panzhenfeng on 2016/8/24.
 */
public class ServerUseSearchTreasureTool extends PlayerPacket {
    private int itemId = 0;
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        itemId = buff.readInt();
    }

    @Override
    public void execPacket(Player player) {
        SearchTreasureModule searchTreasureModule = module(MConst.SearchTreasure);
        ToolModule toolModule = (ToolModule)module(MConst.Tool);
        //先移除再使用;
        if(searchTreasureModule.getRecordMapSearchTreasure().removeItem(itemId, 1)){
            toolModule.useToolNoInBagByItemId(itemId, 1);
            searchTreasureModule.getRecordMapSearchTreasure().syncItemDatasToClient();
        }else{
 //           searchTreasureModule.warn(I18n.get("searchtreasure.istoolnothas"));
        }
    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.S_SEARCHTREASURE_USETOOL;
    }
}
