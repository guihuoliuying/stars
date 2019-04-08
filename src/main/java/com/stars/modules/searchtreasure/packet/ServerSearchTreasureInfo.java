package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.searchtreasure.SearchTreasureConstant;
import com.stars.modules.searchtreasure.SearchTreasureManager;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端请求查看探宝的玩家信息;
 * Created by panzhenfeng on 2016/8/24.
 */
public class ServerSearchTreasureInfo extends PlayerPacket {
    private byte oprType;
    private int requestMapId;
    //玩家剩余血量;
    private int myselfRemainHp;
    //怪物剩余血量map;
    private HashMap<String, Integer> monsterUidRemainHpDic;
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        oprType = buff.readByte();
        switch (oprType){
            case ClientSearchTreasureInfo.TYPE_GET_AWARD:
                requestMapId = buff.readInt();
                break;
            case ClientSearchTreasureInfo.TYPE_SELECT_MAPID:
                requestMapId = buff.readInt();
                break;
            case ClientSearchTreasureInfo.TYPE_SYNC_HP:
                monsterUidRemainHpDic = new HashMap<>();
                myselfRemainHp = buff.readInt();
                int monsterSize = buff.readInt();
                String monsterUid;
                int monsterRemainHp;
                for (int i = 0; i<monsterSize; i++){
                    monsterUid = buff.readString();
                    monsterRemainHp = buff.readInt();
                    monsterUidRemainHpDic.put(monsterUid, monsterRemainHp);
                }
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule)module(MConst.SearchTreasure);
        ClientSearchTreasureInfo clientSearchTreasureInfo = null;
        switch (oprType){
            case ClientSearchTreasureInfo.TYPE_SELECT_MAPID:
                //判断是否有mapId>0先;
                int remainInCount = searchTreasureModule.getRecordMapSearchTreasure().getDailyRemainInCount();
                if(remainInCount <= 0){
                    searchTreasureModule.warn(I18n.get("searchtreasure.noenoughtimes"));
                    return;
                }
                if(searchTreasureModule.getRecordMapSearchTreasure().getMapId() == 0){
                    searchTreasureModule.getRecordMapSearchTreasure().setNewSearchMapId(requestMapId, false, false, false);
                    searchTreasureModule.dispatchDailyEvent();
                }else{
                    LogUtil.error("SearchTreasure has set mapId = "+searchTreasureModule.getRecordMapSearchTreasure().getMapId());
                }
                break;
            case ClientSearchTreasureInfo.TYPE_INFO:
                clientSearchTreasureInfo = new ClientSearchTreasureInfo(searchTreasureModule.getRecordMapSearchTreasure());
                clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_INFO);
                PlayerUtil.send(getRoleId(), clientSearchTreasureInfo);
                break;
            case  ClientSearchTreasureInfo.TYPE_SYNC_HP:
                //更新玩家血量的存储;
                searchTreasureModule.getRecordMapSearchTreasure().setRemainHp(myselfRemainHp);
                //更新怪物血量的存储;
                searchTreasureModule.getRecordMapSearchTreasure().setCurProducedMonsterRemainHp(monsterUidRemainHpDic);
                break;
            case ClientSearchTreasureInfo.TYPE_GET_AWARD:
                //判断当前地图是否已经探索完成;
                byte mapState = searchTreasureModule.getRecordMapSearchTreasure().getMapState(requestMapId);
                if(mapState == SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_NOGET){
                    //获取请求地图的奖励数据;
                    SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(requestMapId);
                    if(searchMapVo != null){
                        ToolModule toolModule = (ToolModule)module(MConst.Tool);
                        Map<Integer,Integer> awardMap = toolModule.addAndSend(searchMapVo.getMapReward(), MConst.CCSearchTreasure,EventType.SEARCHTRESURE.getCode());
                        //TODO 这里要判断下是否下发id状态到客户端, 这里就不下发这个状态了;
                        searchTreasureModule.getRecordMapSearchTreasure().setMapState(requestMapId, SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED, false);
                        searchTreasureModule.getRecordMapSearchTreasure().setNewSearchMapId(0, false, false, false);
                        searchTreasureModule.getRecordMapSearchTreasure().setMapId0();
                        //发送奖励数据给客户端进行显示;
                        clientSearchTreasureInfo = new ClientSearchTreasureInfo();
                        clientSearchTreasureInfo.setItemIdCount(awardMap);
                        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_MAPREWARDS);
                        searchTreasureModule.send(clientSearchTreasureInfo);
                        //发送地图状态;
                        ClientSearchTreasureInfo clientSearchTreasureInfo2 = new ClientSearchTreasureInfo(searchTreasureModule.getRecordMapSearchTreasure());
                        clientSearchTreasureInfo2.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_MAP);
                        clientSearchTreasureInfo2.setMapId(0);
                        searchTreasureModule.send(clientSearchTreasureInfo2);
                        //同步信息;
                        ClientSearchTreasureInfo clientSearchTreasureInfo3 = new ClientSearchTreasureInfo(searchTreasureModule.getRecordMapSearchTreasure());
                        clientSearchTreasureInfo3.setOprType(ClientSearchTreasureInfo.TYPE_INFO);
                        searchTreasureModule.send(clientSearchTreasureInfo3);
                    }else{
                        searchTreasureModule.warn(I18n.get("searchtreasure.ismapnotexist"));
                        return;
                    }
                }else{
                    searchTreasureModule.warn(I18n.get("searchtreasure.isnotenoughtoreward"));
                    return;
                }
                break;
        }
    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.S_SEARCHTREASURE_INFO;
    }
}