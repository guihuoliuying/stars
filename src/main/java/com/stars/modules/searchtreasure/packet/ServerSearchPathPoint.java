package com.stars.modules.searchtreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.SearchTreasureScene;
import com.stars.modules.searchtreasure.SearchTreasureConstant;
import com.stars.modules.searchtreasure.SearchTreasureManager;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.SearchTreasurePacketSet;
import com.stars.modules.searchtreasure.prodata.SearchContentVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.modules.searchtreasure.recordmap.RecordMapSearchTreasure;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.I18n;

import java.util.Map;

/**
 * 客户端请求查看探索点的content;
 * Created by panzhenfeng on 2016/8/24.
 */
public class ServerSearchPathPoint extends PlayerPacket {
    public final static byte REQUEST_TYPE = 0; //请求数据;
    public final static byte PICKUP_TYPE = 1; //拾取;
    public final static byte GET_AWARD_TYPE = 3; //领取奖励;
    private byte type = REQUEST_TYPE; //区分协议的作用,0请求数据,1使用(当怪物击杀时无需走这个协议,宝箱,传送点就需要)
    private float posX;
    private float posY;
    private float posZ;
    private int pathPointIndex = 0;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        type = buff.readByte();
        if(type != GET_AWARD_TYPE){
            pathPointIndex = buff.readInt();
            posX = Float.parseFloat(buff.readString());
            posY = Float.parseFloat(buff.readString());
            posZ = Float.parseFloat(buff.readString());
        }
    }

    @Override
    public void execPacket(Player player) {
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) module(MConst.SearchTreasure);
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        //判断当前探宝图是否在运作中;
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        Map<Integer, Integer> normalAward;
        if (!(sceneModule.getScene() instanceof SearchTreasureScene)) {
            searchTreasureModule.warn(I18n.get("searchtreasure.isnotinmap"));
            return;
        }
        if (((FightScene)sceneModule.getScene()).stageStatus != SceneManager.STAGE_PROCEEDING) {
            searchTreasureModule.warn(I18n.get("searchtreasure.ismapnotrunning"));
            return;
        }
        switch (this.type){
            case GET_AWARD_TYPE://领取奖励类型;
                recordMapSearchTreasure.manualGetAward();
                break;
            case PICKUP_TYPE://拾取类型;
                //要去遍历探宝图中的所剩可拾取物品;
                int pickupIndex = recordMapSearchTreasure.pickup(posX, posZ);
                if(pickupIndex<0){
                    searchTreasureModule.warn(I18n.get("searchtreasure.isnoboxinhere"));
                }
                break;
            default:
                //判断当前层是否已经完成了;
                if(SearchTreasureConstant.getStateIsComplete(recordMapSearchTreasure.getStageSearchState())){
                    searchTreasureModule.warn(I18n.get("searchtreasure.searcompletecanoprpoint"));
                    return;
                }
                //判断当前请求的探索点是否合法;
                if (recordMapSearchTreasure.getPathPointIndex() == pathPointIndex) {
                    if (this.type == REQUEST_TYPE) { //请求类型;
                        //判断当前探索点是否已经请求过随机数据了;
                        if(SearchTreasureConstant.getStateIsComplete(recordMapSearchTreasure.getPathPointState())){
                            searchTreasureModule.warn(I18n.get("searchtreasure.aleadyrandomeddata"));
                            return;
                        }
                        //判断下位置是否合法;
                        SearchStageVo searchStageVo = recordMapSearchTreasure.getCurSearchStageVo();
                        if (searchStageVo.isInPathPoint(pathPointIndex, posX, posZ)) {
                            int contentId = recordMapSearchTreasure.getCurSearchStageContentId(pathPointIndex);
                            SearchContentVo searchContentVo = SearchTreasureManager.getSearchContentVo(contentId);
                            ClientSearchPathPoint clientSearchPathPoint = null;
                            switch (searchContentVo.getType()) {
                                case SearchTreasureConstant.CONTENTTYPE_MONSTER:
                                    SearchTreasureScene searchTreasureScene = (SearchTreasureScene) sceneModule.getScene();
                                    searchTreasureScene.spawnMonsterAndSendToClient(moduleMap(), posX*10, posY*10, posZ*10);
                                    recordMapSearchTreasure.setPathPointState(SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_NOGET);
                                    //通知客户端已经开始刷怪了;
                                    clientSearchPathPoint = new ClientSearchPathPoint(SearchTreasureConstant.CONTENTTYPE_MONSTER);
                                    PlayerUtil.send(getRoleId(), clientSearchPathPoint);
                                    break;
                                case SearchTreasureConstant.CONTENTTYPE_REWARD: //领取奖励;
                                    DropModule dropModule = (DropModule) module(MConst.Drop);
                                    int dropId = Integer.parseInt(searchContentVo.getParam());
                                    normalAward = dropModule.executeDrop(dropId, 1,false);
                                    recordMapSearchTreasure.addCacheAward(normalAward);
                                    recordMapSearchTreasure.setPathPointState(SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_NOGET);
                                    //通知客户端奖励数据,用于手动领取;
                                    clientSearchPathPoint = new ClientSearchPathPoint(SearchTreasureConstant.CONTENTTYPE_REWARD);
                                    clientSearchPathPoint.setAwardMap(recordMapSearchTreasure.getCacheItemMap());
                                    PlayerUtil.send(getRoleId(), clientSearchPathPoint);
                                    recordMapSearchTreasure.setWaitManualGetAward(true);
                                    break;
                            }
                        }else{
//                            searchTreasureModule.warn("请求的探索点不合法, 不在检查范围内:" + recordMapSearchTreasure.getPathPointIndex());
                            searchTreasureModule.warn(I18n.get("searchtreasure.illegalopr"));
                        }
                    }
                } else {
//                    searchTreasureModule.warn("请求的探索点不合法, 服务器记录的是:" + recordMapSearchTreasure.getPathPointIndex());
                    searchTreasureModule.warn(I18n.get("searchtreasure.illegalopr"));
                }
                break;
        }
    }

    @Override
    public short getType() {
        return SearchTreasurePacketSet.S_SEARCHTREASURE_PATH_POINT;
    }
}