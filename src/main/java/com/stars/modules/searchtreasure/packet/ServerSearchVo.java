package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.searchtreasure.SearchTreasureManager;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求探宝vo数据;
 * Created by panzhenfeng on 2016/8/24.
 */
public class ServerSearchVo extends PlayerPacket {
    public final static byte MAPVO = 1;
    public final static byte STAGEVO = 2;

    private byte voType;
    private int startSearchMapId;
    private int searchCount;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        voType = buff.readByte();
        startSearchMapId = buff.readInt();
        searchCount = buff.readInt();
    }


    @Override
    public void execPacket(Player player) {
        SearchTreasureModule searchTreasureModule = module(MConst.SearchTreasure);
        ClientSearchVo clientSearchVo = new ClientSearchVo(voType, startSearchMapId, searchTreasureModule.getRecordMapSearchTreasure());
        switch (voType) {
            case MAPVO:
                startSearchMapId = startSearchMapId == 0 ? SearchTreasureManager.getFirstSearchMapVo().getMapId() : startSearchMapId;
                SearchMapVo searchMapVo = null;
                searchCount = searchCount < 0 ? SearchTreasureManager.getSearchMapListCount() : searchCount;
                for (int i = 0; i < searchCount; i++) {
                    searchMapVo = SearchTreasureManager.getSearchMapVo(startSearchMapId + i);
                    clientSearchVo.addSearchMapVo(searchMapVo);
                }
                PlayerUtil.send(getRoleId(), clientSearchVo);
                break;
            case STAGEVO:
                SearchStageVo searchStageVo = null;
                searchMapVo = SearchTreasureManager.getSearchMapVo(startSearchMapId);
                if(searchMapVo != null){
                    String[] searchStageArr = searchMapVo.getSearchStages().split("\\+");
                    for (int i = 0, len = searchStageArr.length; i < len; i++) {
                        searchStageVo = SearchTreasureManager.getSearchStageVo(Integer.parseInt(searchStageArr[i]));
                        clientSearchVo.addSearchStageVo(searchStageVo);
                    }
                }
                PlayerUtil.send(getRoleId(), clientSearchVo);
                break;
        }
    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.S_SEARCHTREASURE_VO;
    }
}

