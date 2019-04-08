package com.stars.modules.searchtreasure.recordmap;

import com.stars.core.attr.Attr;
import com.stars.core.attr.FormularUtils;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.searchtreasure.SearchTreasureConstant;
import com.stars.modules.searchtreasure.SearchTreasureManager;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.packet.ClientSearchTreasureInfo;
import com.stars.modules.searchtreasure.prodata.SearchContentVo;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * 角色的仙山探宝数据;
 * Created by panzhenfeng on 2016/8/24.
 */
public class RecordMapSearchTreasure {
    /**注意：每一块的数据都由逗号进行分割;*/
    /**
     * 记录的时间;
     */
    private String recordTime;
    /**
     * 剩余血量;
     */
    private float remainHpRate = 1.0f;
    /**
     * 当前所处地图ID;
     */
    private int mapId;
    /**
     * 当前搜索的地图状态;
     */
    private byte mapSearchState;
    /**
     * 剩余宝箱组; 注意,要使用setter/getter进行访问;
     */
    private String chestInfos;
    /**
     * 当前搜索的层ID;
     */
    private int searchStageIndex;
    /**
     * 当前搜索层的探索点权重信息, serial1+contentId1+serial2+contentId2..;
     */
    private String searchStagePPRateInfo;
    /**
     * 当前搜索的层状态;
     */
    private byte stageSearchState;
    /**
     * 当前探索点索引,从1开始;
     */
    private int pathPointIndex;
    /**
     * 当前探索点的状态;
     */
    private byte pathPointState;
    /**
     * 当前探索点选中的contentId;
     */
    private int contentId;
    /**
     * content里的参数，可能是dropId或者monsterId
     */
    private String contentParams;
    /**
     * 道具; 注意,要使用setter/getter进行访问;
     */
    private String items;
    /**
     * 所有地图的状态;
     */
    private String allMapStateStr;
    /**
     * 缓存奖励;
     */
    private String cacheItemStr;
    /**
     * 缓存当前刷出的怪;<monsterUid, string>
     */
    private Map<String, String> curProducedMonsters = null;
    /**
     * 缓存当前刷出的怪的剩余血量;
     */
    private Map<String, Integer> curProducedMonstersRemainHp = new HashMap<>();

    private String[] mapSearchStageIds = null;
    private List<String> chestList = new ArrayList<>();
    private List<String> itemList = new ArrayList<>();
    private List<String> allMapStateList = new ArrayList<>();
    private float boxRadius = 0;
    /**
     * 缓存奖励
     */
    private Map<Integer, Integer> cacheItemMap = new HashMap<>();

    protected ModuleContext context;
    protected RecordMap recordMap;
    private Map<String, Module> moduleMap;
    private int dailyTotalCount = 0;
    private int dailyRemainCount = 0;
    private boolean isWaitManualGetAward = false;

    public RecordMapSearchTreasure(ModuleContext context, Map<String, Module> stringModuleMap) {
        this.context = context;
        this.moduleMap = stringModuleMap;
        this.recordMap = context.recordMap();
        dailyTotalCount = DataManager.getCommConfig("searchtreasure_searchtimes", 0);
        initRecordMapData();
        String tmpValue = DataManager.getCommConfig("searchtreasure_chest_range");
        if (tmpValue.split("\\+").length > 1) {
            tmpValue = tmpValue.split("\\+")[1];
        }
        boxRadius = Float.parseFloat(tmpValue) / 10; //因为策划填的是分米;
    }

    private void initRecordMapData() {
        decodeData();
//        checkReset();
    }

    public void onDailyReset() {
        resetReviveNum();
        setDailyRemainCount(dailyTotalCount);
//        clearMapProcess();
//        setMapId0();
    }

    private void setDailyRemainCount(int value) {
        dailyRemainCount = value;
        recordMap.setString("searchtreasure.dailyremaincount", String.valueOf(dailyRemainCount));
    }

    public void setHasSearchOnce() {
        setDailyRemainCount(dailyRemainCount - 1);
    }

    public int getDailyRemainInCount() {
        return dailyRemainCount;
    }

    public int getDailyTotalCount() {
        return dailyTotalCount;
    }

    /**
     * 发送日常的剩余进入次数;
     *
     * @param buff
     */
    public void writeDailyRemainInCount(NewByteBuffer buff) {
        int totalCount = getDailyTotalCount();
        int alreadyInCount = totalCount - getDailyRemainInCount();
        buff.writeInt(totalCount);
        buff.writeInt(alreadyInCount);
    }

    /**
     * 外部调用接口
     */
    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(this.mapId);
        buff.writeByte(this.mapSearchState);
        writeDailyRemainInCount(buff);
        buff.writeInt(this.getAlreadyReliveCount());
        if (!SearchTreasureConstant.getStateIsComplete(this.mapSearchState)) {
            buff.writeString(this.getChestInfos());
            if (this.searchStageIndex < 0) {
                buff.writeInt(0);
                buff.writeByte(SearchTreasureConstant.SEARCH_PROCESS_ING);
            } else {
                buff.writeInt(this.searchStageIndex);
                buff.writeByte(this.stageSearchState);
            }
            buff.writeInt(this.getRemainHp());
            writePathPointParamBuff(buff);
            writItemToBuff(buff);
            writeCacheAwardBuff(buff);
        }
    }

    private void writItemToBuff(NewByteBuffer buff) {
        buff.writeInt(itemList.size());
        String[] arr = null;
        for (int i = 0, len = itemList.size(); i < len; i++) {
            arr = itemList.get(i).split("\\+");
            if (arr.length > 1) {
                buff.writeInt(i + 1);
                buff.writeInt(Integer.parseInt(arr[0]));
                buff.writeInt(Integer.parseInt(arr[1]));
            }
        }
    }

    private void writeCacheAwardBuff(NewByteBuffer buff) {
        buff.writeInt(this.cacheItemMap.size());
        Set<Integer> keyset = this.cacheItemMap.keySet();
        for (Integer key : keyset) {
            buff.writeInt(key);
            buff.writeInt(this.cacheItemMap.get(key));
        }
    }

    public void writePathPointParamBuff(NewByteBuffer buff) {
        buff.writeInt(this.pathPointIndex);
        buff.writeByte(this.pathPointState);
        String pathPointParam = "";
        if (this.getCurSearchStageVo() != null) {
            pathPointParam = this.getCurSearchStageVo().getPathPointInfo(this.pathPointIndex);
        }
        buff.writeString(pathPointParam);
    }

    private void addCacheAwardSingle(int itemId, int count) {
        int value = 0;
        if (cacheItemMap.containsKey(itemId)) {
            value = cacheItemMap.get(itemId);
        }
        cacheItemMap.put(itemId, value + count);
    }

    /**
     * 添加缓存奖励
     */
    public void addCacheAward(Map<Integer, Integer> map) {
        int itemId;
        int count;
        DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            itemId = entry.getKey();
            count = entry.getValue();
            //判断下是不是item先;
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo != null) {
                addCacheAwardSingle(itemId, count);
            } else {
                DropVo dropVo = DropManager.getDropVo(itemId);
                if (dropVo != null) {
                    Map<Integer, Integer> rewardMap = dropModule.executeDrop(itemId, count, false);
                    for (Map.Entry<Integer, Integer> kvp : rewardMap.entrySet()) {
                        addCacheAwardSingle(kvp.getKey(), kvp.getValue());
                    }
                }
            }
        }
    }


    /**
     * 拾取;
     *
     * @return
     */
    public int pickup(float x, float y) {
        String[] chestArr = null;
        float centerX;
        float centerY;
        for (int i = chestList.size() - 1; i >= 0; i--) {
            chestArr = chestList.get(i).split("\\+");
            centerX = Float.parseFloat(chestArr[0]) / 10;
            centerY = Float.parseFloat(chestArr[1]) / 10;
            //判断哪个在范围内, 一次最多只能捡一个;
            if (FormularUtils.isPointInCircle(centerX, centerY, boxRadius, x, y)) {
                //获取掉落奖励;
                ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
                DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
                Map<Integer, Integer> rewardMap = dropModule.executeDrop(Integer.parseInt(chestArr[2]), 1, true);
                Map<Integer, Integer> searchTreasureMap = SearchTreasureManager.filterSearchTreasureItemIds(rewardMap);
                //发送道具;
//                ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo();
//                clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_ITEM);
//                clientSearchTreasureInfo.setItemIdCount(rewardMap);
//                toolModule.send(clientSearchTreasureInfo);

                toolModule.addAndSend(rewardMap, MConst.CCSearchTreasure, EventType.SEARCHTRESURE.getCode());
                addItem(searchTreasureMap);
                //移除该宝箱信息;
                removeChestAt(i);
                return i;
            }
        }
        return -1;
    }

    /**
     * 清除地图的进度;
     */
    public void clearMapProcess() {
        setNewSearchMapId(this.mapId, true);
    }

//    /**
//     * 需要重置时执行, 外部调用前要判断下是否与recordTime有差异;
//     */
//    public void checkReset() {
//        this.recordTime = DateUtil.getDateStr();
//    }

    public void resetReviveNum() {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        roleModule.getRoleRow().resetReviveNum(SceneManager.SCENETYPE_SEARCHTREASURE);
        this.updateAlreadyReliveCount();
    }

    public void setMapId0() {
        this.mapId = 0;
        this.setMapSearchState(SearchTreasureConstant.SEARCH_PROCESS_NONE, false);
        encodeData();
    }

    public void setMapId0AndFinish() {
        clearMapProcess();
        setMapId0();
        encodeData();
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        searchTreasureModule.sendInfo();
    }

    public void setNewSearchMapId(int mapId, boolean isReset) {
        setNewSearchMapId(mapId, isReset, true, true);
    }

    /**
     * 设置地图ID;
     */
    public void setNewSearchMapId(int mapId, boolean isReset, boolean needSyncToClient, boolean needSyncProcessIngToClient) {
        SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(mapId);
        RoleModule roleModule = ((RoleModule) moduleMap.get(MConst.Role));
        this.mapId = mapId;
        //血量在进入新地图时设置;
        setRemainHp(roleModule.getRoleRow().getTotalAttr().getHp());
//        String[] tmpStageArr = searchMapVo.getSearchStages().split("\\+");
        //设置新的地图层;
        this.searchStageIndex = -1;
        if (searchMapVo != null) {
            mapSearchStageIds = searchMapVo.getSearchStages().split("\\+");
            setNextSearchStageIndex(needSyncToClient, needSyncProcessIngToClient);
            this.setMapState(mapId, isReset ? SearchTreasureConstant.SEARCH_PROCESS_NONE : SearchTreasureConstant.SEARCH_PROCESS_ING, true);
        }
        this.setItems("");
        this.setCacheItemStr("");
        this.setCurProducedMonsters("");
        this.setCurProducedMonsterRemainHpString("");
//        checkReset();
        resetReviveNum();
        encodeData();
    }

    public void setNextSearchStageIndex(boolean synPathPointIndexToClient) {
        setNextSearchStageIndex(synPathPointIndexToClient, true);
    }

    public void setNextSearchStageIndex(boolean synPathPointIndexToClient, boolean syncProcessIngToClient) {
        //判断是不是最后一个层次了;
        SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(this.mapId);
        if (searchMapVo != null) {
            int newSearchStageIndex = searchMapVo.getNextStageIndex(this.searchStageIndex);
            if (newSearchStageIndex != this.searchStageIndex) {
                setSearchStageIndex(newSearchStageIndex);
                setPathPointIndex(1, synPathPointIndexToClient);
                randomCurSearchStagePPRate();
                setStageSearchState(SearchTreasureConstant.SEARCH_PROCESS_NONE, false);
                setContentId(0);
                setContentParams("");
                SearchStageVo searchStageVo = SearchTreasureManager.getSearchStageVo(searchMapVo.getStageIdByIndex(this.searchStageIndex));
                this.setChestInfos(searchStageVo.getChest());
                setStageSearchState(SearchTreasureConstant.SEARCH_PROCESS_ING, syncProcessIngToClient);
                encodeData();
            }
        }
    }

    public void setSearchStageIndex(int searchStageIndex) {
        this.searchStageIndex = searchStageIndex;
        recordMap.setString("searchtreasure.searchStageIndex", Integer.toString(this.searchStageIndex));
    }

    /**
     * 设置地图状态,并同步到客户端;
     *
     * @param mapId
     * @param state
     */
    public void setMapState(int mapId, byte state, boolean needSyncToClient) {
        String newValue = mapId + "+" + state;
        String[] tmpArr;
        do {
            boolean hasFind = false;
            for (int i = 0, len = allMapStateList.size(); i < len; i++) {
                tmpArr = allMapStateList.get(i).split("\\+");
                if (Integer.parseInt(tmpArr[0]) == mapId) {
                    allMapStateList.set(i, newValue);
                    hasFind = true;
                    break;
                }
            }
            if (!hasFind) {
                allMapStateList.add(newValue);
            }
        } while (false);
        //判断是不是当前地图ID;
        if (this.mapId == mapId) {
            setMapSearchState(state, needSyncToClient);
        }
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        searchTreasureModule.updateRedPoints();
        encodeData();
    }

    /**
     * 获取地图的状态;
     *
     * @param mapId
     * @return
     */
    public byte getMapState(int mapId) {
        String[] tmpArr;
        for (int i = 0, len = allMapStateList.size(); i < len; i++) {
            tmpArr = allMapStateList.get(i).split("\\+");
            if (Integer.parseInt(tmpArr[0]) == mapId) {
                return Byte.parseByte(tmpArr[1]);
            }
        }
        return SearchTreasureConstant.SEARCH_PROCESS_NONE;
    }

    //通过状态,获取当前记录的地图id组
    public List<Integer> getAllMapIdsByState(byte state) {
        List<Integer> rtnMapIdList = new ArrayList<>();
        String[] tmpArr;
        for (int i = 0, len = allMapStateList.size(); i < len; i++) {
            tmpArr = allMapStateList.get(i).split("\\+");
            if (Byte.parseByte(tmpArr[1]) == state) {
                rtnMapIdList.add(Integer.parseInt(tmpArr[0]));
            }
        }
        return rtnMapIdList;
    }

    private void decodeData() {
        String stateNoneStr = Byte.toString(SearchTreasureConstant.SEARCH_PROCESS_NONE);
        this.remainHpRate = Float.parseFloat(recordMap.getString("searchtreasure.remainHp", "0"));
        if (this.remainHpRate > 1) {
            this.remainHpRate = 1;
        } else if (this.remainHpRate < 0) {
            this.remainHpRate = 0;
        }
        this.mapId = Integer.parseInt(recordMap.getString("searchtreasure.mapId", "0"));
        this.mapSearchState = Byte.parseByte(recordMap.getString("searchtreasure.mapSearchState", stateNoneStr));
        this.setChestInfos(recordMap.getString("searchtreasure.chestinfo", ""));
        this.searchStageIndex = Integer.parseInt(recordMap.getString("searchtreasure.searchStageIndex", "-1"));
        this.searchStagePPRateInfo = recordMap.getString("searchtreasure.searchStagePPRateInfo", "");
        this.stageSearchState = Byte.parseByte(recordMap.getString("searchtreasure.stageSearchState", stateNoneStr));
        this.pathPointIndex = Integer.parseInt(recordMap.getString("searchtreasure.pathPointIndex", "-1"));
        this.pathPointState = Byte.parseByte(recordMap.getString("searchtreasure.pathPointState", stateNoneStr));
        this.contentId = Integer.parseInt(recordMap.getString("searchtreasure.contentId", "0"));
        this.contentParams = recordMap.getString("searchtreasure.contentParams", "");
        this.setItems(recordMap.getString("searchtreasure.items", ""));
        SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(this.mapId);
        if (searchMapVo != null) {
            mapSearchStageIds = searchMapVo.getSearchStages().split("\\+");
        }
        this.setAllMapStateStr(recordMap.getString("searchtreasure.allmapstatestr", "0"));
        this.setCacheItemStr(recordMap.getString("searchtreasure.cacheitemstr", ""));
        this.setCurProducedMonsters(recordMap.getString("searchtreasure.curproducedmonsters", ""));
        this.setDailyRemainCount(Integer.parseInt(recordMap.getString("searchtreasure.dailyremaincount", String.valueOf(getDailyTotalCount()))));
        this.setCurProducedMonsterRemainHpString(recordMap.getString("searchtreasure.curproducedmonstersremainhp", ""));
        //stageSearchState为3，但是searchStageIndex不为-1或5(最大层前一层)的，视为脏数据，会导致玩家无法继续,需要修复数据
        if (this.stageSearchState == SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED
                && this.searchStageIndex != -1 && this.searchStageIndex != (mapSearchStageIds.length - 1)) {
            this.setStageSearchState(SearchTreasureConstant.SEARCH_PROCESS_ING, false);
            this.setPathPointIndex(1, false);
            this.setPathPointState(SearchTreasureConstant.SEARCH_PROCESS_NONE);
            com.stars.util.LogUtil.info("六国寻宝玩家脏数据修复");
        }
    }

    //由外部调用,内部调用单一setString即可;
    public void encodeData() {
        recordMap.setString("searchtreasure.remainHp", Float.toString(this.remainHpRate));
        recordMap.setString("searchtreasure.mapId", Integer.toString(this.mapId));
        recordMap.setString("searchtreasure.mapSearchState", Byte.toString(this.mapSearchState));
        recordMap.setString("searchtreasure.chestinfo", this.getChestInfos());
        recordMap.setString("searchtreasure.searchStageIndex", Integer.toString(this.searchStageIndex));
        recordMap.setString("searchtreasure.searchStagePPRateInfo", this.searchStagePPRateInfo);
        recordMap.setString("searchtreasure.stageSearchState", Byte.toString(this.stageSearchState));
        recordMap.setString("searchtreasure.pathPointIndex", Integer.toString(this.pathPointIndex));
        recordMap.setString("searchtreasure.pathPointState", Byte.toString(this.pathPointState));
        recordMap.setString("searchtreasure.contentId", Integer.toString(this.contentId));
        recordMap.setString("searchtreasure.contentParams", this.contentParams);
        recordMap.setString("searchtreasure.items", this.getItems());
        recordMap.setString("searchtreasure.allmapstatestr", this.getAllMapStateStr());
        recordMap.setString("searchtreasure.cacheitemstr", this.getCacheItemStr());
        recordMap.setString("searchtreasure.curproducedmonsters", this.getCurProducedMonstersString());
        recordMap.setString("searchtreasure.dailyremaincount", String.valueOf(this.getDailyRemainInCount()));
        recordMap.setString("searchtreasure.curproducedmonstersremainhp", this.getCurProducedMonstersRemainHpString());
    }


    /**
     * 随机计算当前搜索层的探索点权重,避免每次进入都要计算一次;
     * 计算的结果为serial1+contentId1+serial2+contentId2..
     */
    private void randomCurSearchStagePPRate() {
        SearchStageVo searchStageVo = getCurSearchStageVo();
        String[] tmpArr = searchStageVo.getSearchPointContent().split("\\|");
        String[] serialPPArr = null;
        String[] ppItemArr = null;
        String[] contentRateArr = null;
        StringBuilder sb = new StringBuilder();
        int totalRate = 0;
        int selectContentId = 0;
        for (int i = 0, len = tmpArr.length; i < len; i++) {
            serialPPArr = tmpArr[i].split("=");
            sb.append(serialPPArr[0]);
            sb.append("+");
            if (serialPPArr.length > 1) {
                //计算权重和;
                ppItemArr = serialPPArr[1].split(",");
                for (int k = 0, klen = ppItemArr.length; k < klen; k++) {
                    contentRateArr = ppItemArr[k].split("\\+");
                    totalRate = Integer.parseInt(contentRateArr[1]);
                }
                //随机权重值;
                if (totalRate <= 0) {
                    totalRate = 1; // 防错处理
                }
                int randomValue = new Random().nextInt(totalRate) + 1;
                //获取对应的contentId;
                for (int k = 0, klen = ppItemArr.length; k < klen; k++) {
                    contentRateArr = ppItemArr[k].split("\\+");
                    if (randomValue < Integer.parseInt(contentRateArr[1])) {
                        selectContentId = Integer.parseInt(contentRateArr[0]);
                        break;
                    }
                    randomValue -= Integer.parseInt(contentRateArr[1]);
                }
                sb.append(selectContentId);
            } else {
                sb.append(0);
            }
            if (i + 1 < len) {
                sb.append("+");
            }
        }
        this.searchStagePPRateInfo = sb.toString();
        recordMap.setString("searchtreasure.searchStagePPRateInfo", this.searchStagePPRateInfo);
    }

    /**
     * 获取当前地图层的探索点的contentId;
     */
    public int getCurSearchStageContentId(int ppIndex) {
        String[] tmpArr = this.searchStagePPRateInfo.split("\\+");
        for (int i = 0, len = tmpArr.length; i < len; i += 2) {
            if (ppIndex == Integer.parseInt(tmpArr[i])) {
                return Integer.parseInt(tmpArr[i + 1]);
            }
        }
        return -1;
    }

    /**
     * 完成当前探索点,自动下移到下一个探索点索引,如果是最后一个索引，自动调用completeCurSearchStage;
     *
     * @return
     */
    public void completeCurPathPointIndex() {
        int nextPathPointIndex = this.pathPointIndex + 1;
        cacheItemMap.clear();
        //判断是否探索完当前层了;
        SearchStageVo searchStageVo = getCurSearchStageVo();
        if (nextPathPointIndex > searchStageVo.getSearchPointCount()) {
            completeCurSearchStage();
        } else {
            setPathPointIndex(nextPathPointIndex, true);
        }
    }

    /**
     * 完成当前的搜索层;
     */
    public boolean completeCurSearchStage() {
        boolean rtnValue = false;
        do {
            if (this.searchStageIndex < 0) {
                break;
            }
            if (this.getStageSearchState() != SearchTreasureConstant.SEARCH_PROCESS_ING) {
                break;
            }
            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            boolean isMapComplete = false;
            //判断是否是当前地图的最后一个searchStage,是的话,当前地图就算探索完成了;
            if (this.searchStageIndex == mapSearchStageIds.length - 1) {
                SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
                searchTreasureModule.fireEvent(new JoinActivityEvent(JoinActivityEvent.SEARCHTREASURE));
                setMapState(this.getMapId(), SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_NOGET, true);
                isMapComplete = true;
            }
            setStageSearchState(SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED, !isMapComplete);
            ((FightScene) sceneModule.getScene()).checkFinish(null);
            if (!isMapComplete) {
                setNextSearchStageIndex(false);
            }
            rtnValue = true;
        } while (false);
        return rtnValue;
    }

    /**
     * 移除某个宝箱;
     *
     * @param index
     */
    private void removeChestAt(int index) {
        chestList.remove(index);
        //记录;
        setChestInfos(getChestInfos());
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo();
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_REMOVE_CHEST);
        clientSearchTreasureInfo.setChestIndex(index + 1);
        searchTreasureModule.send(clientSearchTreasureInfo);
    }

    //手动领取奖励;
    public void manualGetAward() {
        Map<Integer, Integer> normalAward = this.getCacheItemMap();
        Map<Integer, Integer> searchTreasureAwrad = SearchTreasureManager.filterSearchTreasureItemIds(normalAward);
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        toolModule.addAndSend(normalAward, 0, EventType.SEARCHTRESURE.getCode());
        //因为有系数的原因，这里算好再下发给客户端;
        Map<Integer, Integer> convertNormalAward = new HashMap<>();
        Set<Integer> normalAwardKeySet = normalAward.keySet();
        for (Integer key : normalAwardKeySet) {
            if (toolModule.isCoeffTool(key)) {
                convertNormalAward.put(key, toolModule.coeffCount(key, normalAward.get(key)));
            } else {
                convertNormalAward.put(key, normalAward.get(key));
            }
        }
        ClientAward clientAward = new ClientAward(convertNormalAward);
        clientAward.setType((byte) 10);
        toolModule.send(clientAward);
        //要判断这里的类型是什么先, 判断怪物是否击杀完先;
        int curSearchStageContentId = this.getCurSearchStageContentId(this.getPathPointIndex());
        SearchContentVo curSeachContentVo = SearchTreasureManager.getSearchContentVo(curSearchStageContentId);
        if (curSeachContentVo != null) {
            if (curSeachContentVo.getType() == SearchTreasureConstant.CONTENTTYPE_MONSTER) {
                if (!this.isCurProducedMonsterIsEmpty()) {
                    toolModule.warn(I18n.get("searchtreasure.monsternokillall"));
                    return;
                }
            }
        }
        this.completeCurPathPointIndex();
        this.addItem(searchTreasureAwrad);
        this.setWaitManualGetAward(false);
    }


    /**
     * 添加道具;
     *
     * @param itemMap
     */
    public void addItem(Map<Integer, Integer> itemMap) {
        if (itemMap == null || itemMap.size() <= 0) {
            return;
        }
        Set<Integer> keys = itemMap.keySet();
        for (Integer key : keys) {
            addItem(key, itemMap.get(key));
        }
        //记录;
        setItems(getItems());
    }

    //叠加上限判断;
    private int adjustItemCountLimit(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            return 0;
        }
        if (itemVo.getStorage() < count) {
            count = itemVo.getStorage();
        }
        return count;
    }

    /**
     * 同步道具信息到客户端;
     */
    public void syncItemDatasToClient() {
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(this);
        String[] infoArr = null;
        for (int i = 0, len = itemList.size(); i < len; i++) {
            infoArr = itemList.get(i).split("\\+");
            clientSearchTreasureInfo.setItemIdCount(i + 1, Integer.parseInt(infoArr[0]), Integer.parseInt(infoArr[1]));
        }
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_ITEM);
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        searchTreasureModule.send(clientSearchTreasureInfo);
    }


    /**
     * 添加道具;
     *
     * @param itemId
     * @param count
     */
    private void addItem(int itemId, int count) {
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
//        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo();
        //查看是否有相同的道具;
        String[] infoArr = null;
        int setIndex = -1;
        int tmpCount = count;
        //先寻找有没有对应的道具槽,没有的话，再找空槽进行填充;
        for (int i = 0, len = itemList.size(); i < len; i++) {
            infoArr = itemList.get(i).split("\\+");
            if (Integer.parseInt(infoArr[0]) == itemId) {
                setIndex = i;
                tmpCount = (Integer.parseInt(infoArr[1]) + count);
                tmpCount = adjustItemCountLimit(itemId, tmpCount);
                itemList.set(i, itemId + "+" + tmpCount);
                //发送给lua,lua索引从1开始;
//                clientSearchTreasureInfo.setItemIdCount(setIndex + 1, itemId, tmpCount);
                break;
            }
        }
        //再找空槽进行填充;
        if (setIndex < 0) {
            for (int i = 0, len = itemList.size(); i < len; i++) {
                infoArr = itemList.get(i).split("\\+");
                if (Integer.parseInt(infoArr[1]) == 0) {
                    setIndex = i;
                    tmpCount = (Integer.parseInt(infoArr[1]) + count);
                    tmpCount = adjustItemCountLimit(itemId, tmpCount);
                    itemList.set(i, itemId + "+" + tmpCount);
                    //发送给lua,lua索引从1开始;
//                    clientSearchTreasureInfo.setItemIdCount(setIndex + 1, itemId, tmpCount);
                    break;
                }
            }
        }
        //往后进行填充;
        if (setIndex < 0) {
            SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(this.mapId);
            if (itemList.size() < searchMapVo.getItemNum()) {
                setIndex = itemList.size();
                tmpCount = adjustItemCountLimit(itemId, tmpCount);
                itemList.add(itemId + "+" + tmpCount);
                //发送给lua,lua索引从1开始;
//                clientSearchTreasureInfo.setItemIdCount(setIndex + 1, itemId, tmpCount);
            } else {
//                return;
            }
        }
//        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_ITEM);
//        searchTreasureModule.send(clientSearchTreasureInfo);
        syncItemDatasToClient();
    }

    /**
     * 移除道具;
     *
     * @param itemId
     * @param count
     */
    public boolean removeItem(int itemId, int count) {
        //查看是否存在这样的道具;
        String[] infoArr = null;
//        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo();
        for (int i = 0, len = itemList.size(); i < len; i++) {
            infoArr = itemList.get(i).split("\\+");
            if (Integer.parseInt(infoArr[0]) == itemId) {
                //判断数量是否足够;
                if (Integer.parseInt(infoArr[1]) >= count) {
                    int remainCount = Integer.parseInt(infoArr[1]) - count;
                    itemList.set(i, itemId + "+" + remainCount);
                    setItems(getItems());
//                    clientSearchTreasureInfo.setItemIdCount(i + 1, itemId, remainCount);
//                    clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_ITEM);
//                    SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
//                    searchTreasureModule.send(clientSearchTreasureInfo);
                    syncItemDatasToClient();
                    return true;
                }
            }
        }
        return false;
    }

    public String getItems() {
        if (itemList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = itemList.size(); i < len; i++) {
                sb.append(itemList.get(i));
                if (i + 1 < len) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        return "";
    }

    public List<String> getItemlist() {
        return itemList;
    }

    public void setItems(String items) {
        itemList.clear();
        if (StringUtil.isNotEmpty(items)) {
            this.items = items;
            String[] tmpArr = this.items.split(",");
            for (int i = 0, len = tmpArr.length; i < len; i++) {
                this.itemList.add(tmpArr[i]);
            }
        }
        recordMap.setString("searchtreasure.items", items);
    }

    public String getChestInfos() {
        StringBuilder sb = new StringBuilder();
        if (chestList.size() > 0) {
            for (int i = 0, len = chestList.size(); i < len; i++) {
                sb.append(chestList.get(i));
                if (i + 1 < len) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    public void setChestInfos(String chestInfos) {
        this.chestInfos = chestInfos;
        chestList.clear();
        if (StringUtil.isNotEmpty(this.chestInfos) && !chestInfos.equals("0")) {
            String[] tmpArr = chestInfos.split(",");
            for (int i = 0, len = tmpArr.length; i < len; i++) {
                chestList.add(tmpArr[i]);
            }
        }
        recordMap.setString("searchtreasure.chestinfo", chestInfos);
    }

    public void updateAlreadyReliveCount() {
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(this);
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_REMAIN_RELIVECOUNT);
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        searchTreasureModule.send(clientSearchTreasureInfo);
    }

    public void setAllMapStateStr(String str) {
        allMapStateList.clear();
        if (StringUtil.isNotEmpty(str) && !str.equals("0")) {
            String[] mapStateArr = str.split(",");
            String[] tmpArr = null;
            for (int i = 0, len = mapStateArr.length; i < len; i++) {
                allMapStateList.add(mapStateArr[i]);
            }
        }
        recordMap.setString("searchtreasure.allmapstatestr", str);
    }

    public String getAllMapStateStr() {
        StringBuilder sb = new StringBuilder();
        if (allMapStateList.size() > 0) {
            for (int i = 0, len = allMapStateList.size(); i < len; i++) {
                sb.append(allMapStateList.get(i));
                if (i + 1 < len) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    public SearchStageVo getCurSearchStageVo() {
        SearchMapVo searchMapVo = SearchTreasureManager.getSearchMapVo(this.mapId);
        if (searchMapVo != null) {
            SearchStageVo searchStageVo = SearchTreasureManager.getSearchStageVo(searchMapVo.getStageIdByIndex(this.searchStageIndex));
            return searchStageVo;
        }
        return null;
    }

    public void syncToClient(byte oprType) {
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(this);
        clientSearchTreasureInfo.setOprType(oprType);
        ((SearchTreasureModule) moduleMap.get(MConst.SearchTreasure)).send(clientSearchTreasureInfo);
    }

    public Map<Integer, Integer> getCacheItemMap() {
        return cacheItemMap;
    }

    public String getCacheItemStr() {
        StringBuilder sb = new StringBuilder();
        Set<Integer> coll = cacheItemMap.keySet();
        int count = cacheItemMap.size();
        if (count > 0) {
            int index = 0;
            for (int key : coll) {
                sb.append(key);
                sb.append("+");
                sb.append(cacheItemMap.get(key));
                if (index + 1 < count) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        return "";
    }

    public void setCacheItemStr(String cacheItemStr) {
        cacheItemMap.clear();
        if (StringUtil.isNotEmpty(cacheItemStr)) {
            String[] itemArr = cacheItemStr.split(",");
            String[] tmpArr = null;
            for (int i = 0, len = itemArr.length; i < len; i++) {
                tmpArr = itemArr[i].split("\\+");
                if (tmpArr.length > 1) {
                    cacheItemMap.put(Integer.parseInt(tmpArr[0]), Integer.parseInt(tmpArr[1]));
                }
            }
        }
        recordMap.setString("searchtreasure.cacheitemstr", cacheItemStr);
    }

    public byte getPathPointState() {
        return pathPointState;
    }

    public void setPathPointState(byte pathPointState) {
        this.pathPointState = pathPointState;
        recordMap.setString("searchtreasure.pathPointState", Byte.toString(this.pathPointState));
    }

    public int getRemainHp() {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        return (int) (roleModule.getRoleRow().getTotalAttr().get(Attr.MAXHP) * this.remainHpRate);
    }

    public void setRemainHp(int remainHp) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        this.remainHpRate = Float.parseFloat(Integer.toString(remainHp)) / roleModule.getRoleRow().getTotalAttr().get(Attr.MAXHP);
        if (this.remainHpRate > 1) {
            this.remainHpRate = 1;
        } else if (this.remainHpRate < 0) {
            this.remainHpRate = 0;
        }
        recordMap.setString("searchtreasure.remainHp", Float.toString(this.remainHpRate));
    }

    public int getAlreadyReliveCount() {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        return roleModule.getReviveNum(SceneManager.SCENETYPE_SEARCHTREASURE);
    }


    public int getMapId() {
        return mapId;
    }


    public byte getStageSearchState() {
        return stageSearchState;
    }

    public void setStageSearchState(byte stageSearchState, boolean needSyncToClient) {
        this.stageSearchState = stageSearchState;
        recordMap.setString("searchtreasure.stageSearchState", Byte.toString(this.stageSearchState));
        if (needSyncToClient) {
            syncToClient(ClientSearchTreasureInfo.TYPE_SYNC_STAGESTATE);
        }
    }

    public int getPathPointIndex() {
        return pathPointIndex;
    }

    public void setPathPointIndex(int pathPointIndex, boolean synToClient) {
        com.stars.util.LogUtil.info("六国寻宝发送1");
        this.pathPointIndex = pathPointIndex;
        setPathPointState(SearchTreasureConstant.SEARCH_PROCESS_NONE);
        recordMap.setString("searchtreasure.pathPointIndex", Integer.toString(this.pathPointIndex));
        if (synToClient) {
            LogUtil.info("六国寻宝发送2");
            syncToClient(ClientSearchTreasureInfo.TYPE_SYNC_SEARCHPOINT);
        }
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
        recordMap.setString("searchtreasure.contentId", Integer.toString(this.contentId));
    }

    public String getContentParams() {
        return contentParams;
    }

    public void setContentParams(String contentParams) {
        this.contentParams = contentParams;
        recordMap.setString("searchtreasure.contentParams", this.contentParams);
    }

    public byte getMapSearchState() {
        return mapSearchState;
    }

    private void setMapSearchState(byte mapSearchState, boolean needSyncToClient) {
        this.mapSearchState = mapSearchState;
        if (needSyncToClient) {
            ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(this);
            clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_MAP);
            clientSearchTreasureInfo.setMapId(this.mapId);
            SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
            searchTreasureModule.send(clientSearchTreasureInfo);
        }
        recordMap.setString("searchtreasure.mapSearchState", Byte.toString(this.mapSearchState));
    }

    public String getSearchStagePPRateInfo() {
        return searchStagePPRateInfo;
    }

    //怪物相关的存储;
    public void setCurProducedMonsters(Map<String, FighterEntity> monsterMap) {
        //创建出需要存储的数据;
        curProducedMonsters = new HashMap<>();
        FighterEntity monsterEntity;
        if (monsterMap != null) {
            String[] posArr = null;
            for (Map.Entry<String, FighterEntity> kvp : monsterMap.entrySet()) {
                monsterEntity = kvp.getValue();
                StringBuffer sb = new StringBuffer();
                sb.append(monsterEntity.getAttribute().getHp());
                sb.append("+");
                //由于坐标是浮点数，太大容易超出字段存储范围.这里保留一位小数;
                posArr = monsterEntity.getPosition().split("\\+");
                for (int i = 0, len = posArr.length; i < len; i++) {
                    BigDecimal bigDecimal = new BigDecimal(Float.parseFloat(posArr[i]));
                    sb.append(bigDecimal.setScale(1, BigDecimal.ROUND_FLOOR).floatValue());
                    if (i + 1 < len) {
                        sb.append("+");
                    }
                }
//                sb.append(monsterAttributeVo.getPosition());//因为这个也是+号连接的;
                curProducedMonsters.put(kvp.getKey(), sb.toString());
            }
        }
        recordMap.setString("searchtreasure.curproducedmonsters", this.getCurProducedMonstersString());
    }

    public void removeCurProducedMonster(String monsterUid) {
        curProducedMonsters.remove(monsterUid);
        recordMap.setString("searchtreasure.curproducedmonsters", this.getCurProducedMonstersString());
    }

    public Map<String, String> getCurProducedMonsters() {
        return curProducedMonsters;
    }

    public void setCurProducedMonsters(String recordStr) {
        curProducedMonsters = new HashMap<>();
        if (recordStr.equals("0") || StringUtil.isNotEmpty(recordStr)) {
            String[] monsterItems = recordStr.split(",");
            String[] idInfoArr = null;
            for (int i = 0, len = monsterItems.length; i < len; i++) {
                idInfoArr = monsterItems[i].split("=");
                curProducedMonsters.put(idInfoArr[0], idInfoArr[1]);
            }
        }
    }

    public boolean isCurProducedMonsterIsEmpty() {
        return curProducedMonsters.size() <= 0;
    }

    public String getCurProducedMonstersString() {
        StringBuffer sb = new StringBuffer();
        int len = curProducedMonsters.size();
        if (len > 0) {
            int index = 0;
            for (Map.Entry<String, String> kvp : curProducedMonsters.entrySet()) {
                sb.append(kvp.getKey());
                sb.append("=");
                sb.append(kvp.getValue());
                if (index + 1 < len) {
                    sb.append(",");
                }
                index++;
            }
        }
        return sb.toString();
    }

    public void setCurProducedMonsterRemainHp(HashMap<String, Integer> monsterUidRemainHpDic) {
        curProducedMonstersRemainHp.clear();
        for (Map.Entry<String, Integer> uidRemainHp : monsterUidRemainHpDic.entrySet()) {
            if (curProducedMonsters.containsKey(uidRemainHp.getKey())) {
                curProducedMonstersRemainHp.put(uidRemainHp.getKey(), uidRemainHp.getValue());
            }
        }
        recordMap.setString("searchtreasure.curproducedmonstersremainhp", this.getCurProducedMonstersRemainHpString());
    }

    public Map<String, Integer> getCurProducedMonsterRemainHp() {
        return curProducedMonstersRemainHp;
    }

    public void setCurProducedMonsterRemainHpString(String recordStr) {
        curProducedMonstersRemainHp.clear();
        if (StringUtil.isNotEmpty(recordStr)) {
            String[] monsterItems = recordStr.split(",");
            String[] uidRemainHp = null;
            for (int i = 0, len = monsterItems.length; i < len; i++) {
                uidRemainHp = monsterItems[i].split("=");
                curProducedMonstersRemainHp.put(uidRemainHp[0], Integer.parseInt(uidRemainHp[1]));
            }
        }
    }

    public String getCurProducedMonstersRemainHpString() {
        StringBuffer sb = new StringBuffer();
        int len = curProducedMonstersRemainHp.size();
        if (len > 0) {
            int index = 0;
            for (Map.Entry<String, Integer> kvp : curProducedMonstersRemainHp.entrySet()) {
                sb.append(kvp.getKey());
                sb.append("=");
                sb.append(kvp.getValue());
                if (index + 1 < len) {
                    sb.append(",");
                }
                index++;
            }
        }
        return sb.toString();
    }

    public boolean isWaitManualGetAward() {
        return isWaitManualGetAward;
    }

    public void setWaitManualGetAward(boolean isWaitManualGetAward) {
        this.isWaitManualGetAward = isWaitManualGetAward;
    }
}
