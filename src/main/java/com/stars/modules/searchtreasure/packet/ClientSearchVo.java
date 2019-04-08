package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.modules.searchtreasure.recordmap.RecordMapSearchTreasure;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/8/24.
 */
public class ClientSearchVo extends PlayerPacket {
    private byte voType;
    private int requestSearchMapId;
    private List<SearchMapVo> searchMapVoList = null;
    private List<SearchStageVo> searchStageVoList = null;
    private RecordMapSearchTreasure recordMapSearchTreasure;
    public ClientSearchVo() {

    }

    public ClientSearchVo(byte voType, int requestSearchMapId, RecordMapSearchTreasure recordMapSearchTreasure) {
        this.voType = voType;
        this.requestSearchMapId = requestSearchMapId;
        this.recordMapSearchTreasure = recordMapSearchTreasure;
    }

    public void addSearchMapVo(SearchMapVo searchMapVo){
        if(searchMapVoList==null){
            searchMapVoList = new ArrayList<>();
        }
        searchMapVoList.add(searchMapVo);
    }

    public void addSearchStageVo(SearchStageVo searchStageVo){
        if(searchStageVoList==null){
            searchStageVoList = new ArrayList<>();
        }
        searchStageVoList.add(searchStageVo);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.C_SEARCHTREASURE_VO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.voType);
        switch (this.voType){
            case ServerSearchVo.MAPVO:
                SearchMapVo searchMapVo = null;
                buff.writeInt(searchMapVoList.size());
                for(int i = 0, len = searchMapVoList.size(); i<len; i++){
                    searchMapVo = searchMapVoList.get(i);
                    searchMapVo.writeBuff(buff);
                    //获取当前地图探索状态;
                    buff.writeByte(recordMapSearchTreasure.getMapState(searchMapVo.getMapId()));
                }
                break;
            case ServerSearchVo.STAGEVO:
                buff.writeInt(this.requestSearchMapId);
                buff.writeInt(searchStageVoList.size());
                for(int i = 0, len = searchStageVoList.size(); i<len; i++){
                    searchStageVoList.get(i).writeBuffToClientShow(buff);
                }
                break;
        }
    }
}
