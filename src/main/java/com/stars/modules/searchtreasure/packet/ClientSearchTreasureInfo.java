package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.searchtreasure.recordmap.RecordMapSearchTreasure;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应客户端请求查看探宝的玩家信息
 * Created by panzhenfeng on 2016/8/24.
 */
public class ClientSearchTreasureInfo extends PlayerPacket {
    private int chestIndex = 0;
    public final static byte TYPE_INFO = 0;    //类型：所有;
    public final static byte TYPE_SYNC_ITEM = 1;     //类型：同步道具;
    public final static byte TYPE_SYNC_REMAIN_RELIVECOUNT = 2; //类型: 同步剩余免费复活次数;
    public final static byte TYPE_REMOVE_CHEST = 3; //类型：移除宝箱;
    public final static byte TYPE_GET_AWARD = 4;//领取地图完成奖励;
    public final static byte TYPE_SYNC_MAP = 5;//同步地图状态;
    public final static byte TYPE_SYNC_SEARCHPOINT = 6; //同步探索点内容;
    public final static byte TYPE_SYNC_STAGESTATE = 7; //同步探索层状态;
    public final static byte TYPE_SYNC_DAILYCOUNT = 8; //同步今日进入剩余次数;
    public final static byte TYPE_SYNC_MAPREWARDS = 9; //发送地图奖励数据;
    public final static byte TYPE_SYNC_HP = 10; //同步玩家和怪物的血量;
    public final static byte TYPE_SELECT_MAPID = 11; //玩家选择进入的大地图;
    private byte oprType = TYPE_INFO;

    private RecordMapSearchTreasure recordMapSearchTreasure;
    private List<String> itemList;
    private int requestMapId;

    public void setOprType(byte oprType) {
        this.oprType = oprType;
    }

    public ClientSearchTreasureInfo() {

    }

    public ClientSearchTreasureInfo(RecordMapSearchTreasure recordMapSearchTreasure) {
        this.recordMapSearchTreasure = recordMapSearchTreasure;
    }

    public void setItemIdCount(Map<Integer, Integer> itemMap){
        for(Map.Entry<Integer, Integer> kvp : itemMap.entrySet()){
            setItemIdCount(0, kvp.getKey(), kvp.getValue());
        }
    }

    public void setItemIdCount(int itemIndex, int itemId, int itemCount) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        itemList.add(getKey(itemIndex, itemId, itemCount));
    }

    private String getKey(int itemIndex, int itemId, int itemCount){
        return itemIndex + "+" + itemId + "+" + itemCount;
    }

    //调整itemId index的顺序;
    private Map<Integer, String> adjustItemIdIndex(){
        Map<Integer, String> rtnDic = new HashMap<>();
        if (itemList != null){
            String[] infoArr = null;
            int itemIndex = 0;
            int itemId = 0;
            int itemCount = 0;
            for (int i = 0, len = itemList.size(); i<len; i++){
                infoArr = itemList.get(i).split("\\+");
                itemIndex  = Integer.parseInt(infoArr[0]);
                itemId = Integer.parseInt(infoArr[1]);
                itemCount = Integer.parseInt(infoArr[2]);
                if(rtnDic.containsKey(itemId)){
                    infoArr = rtnDic.get(itemId).split("\\+");
                    itemCount += Integer.parseInt(infoArr[2]);
                    rtnDic.put(itemId, getKey(itemIndex, itemId, itemCount));
                }else{
                    rtnDic.put(itemId, itemList.get(i));
                }
            }
        }
        return rtnDic;
    }

    public void setChestIndex(int index) {
        this.chestIndex = index;
    }

    public void setMapId(int mapId) {
        this.requestMapId = mapId;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.C_SEARCHTREASURE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(oprType);
        int length = 0;
        String[] tmpArr = null;
        Map<Integer, String> rewardMap = null;
        switch (oprType) {
            case TYPE_INFO:
                this.recordMapSearchTreasure.writeToBuff(buff);
                break;
            case TYPE_SYNC_MAPREWARDS:
                length = itemList != null ? itemList.size() : 0;
                buff.writeInt(length);
                for (int i = 0; i < length; i++) {
                    tmpArr = itemList.get(i).split("\\+");
                    buff.writeInt(Integer.parseInt(tmpArr[1])); //itemId
                    buff.writeInt(Integer.parseInt(tmpArr[2])); //itemCount
                }
                break;
            case TYPE_SYNC_ITEM:
                rewardMap = adjustItemIdIndex();
                length = rewardMap.size();
                buff.writeInt(length);
                for (Map.Entry<Integer, String> kvp : rewardMap.entrySet()){
                    tmpArr = kvp.getValue().split("\\+");
                    buff.writeInt(Integer.parseInt(tmpArr[0])); //itemIndex
                    buff.writeInt(Integer.parseInt(tmpArr[1])); //itemId
                    buff.writeInt(Integer.parseInt(tmpArr[2])); //itemCount
                }
                break;
            case TYPE_SYNC_REMAIN_RELIVECOUNT:
                buff.writeInt(recordMapSearchTreasure.getAlreadyReliveCount());
                break;
            case TYPE_REMOVE_CHEST:
                buff.writeInt(this.chestIndex);//从1开始;
                break;
            case TYPE_SYNC_MAP:
                buff.writeInt(this.requestMapId);
                buff.writeByte(this.recordMapSearchTreasure.getMapState(this.requestMapId));
                break;
            case TYPE_SYNC_SEARCHPOINT:
                this.recordMapSearchTreasure.writePathPointParamBuff(buff);
                break;
            case TYPE_SYNC_STAGESTATE:
                buff.writeByte(this.recordMapSearchTreasure.getStageSearchState());
                break;
            case TYPE_SYNC_DAILYCOUNT:
                this.recordMapSearchTreasure.writeDailyRemainInCount(buff);
                break;
        }

    }
}
