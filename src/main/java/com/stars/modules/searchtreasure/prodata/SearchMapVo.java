package com.stars.modules.searchtreasure.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 仙山探宝地图表;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchMapVo {
    private int mapId;
    private String name;
    private String searchStages;
    private String mapImg;
    private String mapReward;
    private int levelLimit;
    private int itemNum;
    private String itemFunction;
    private int rebornTimes;
    private String stageitemposition;
    private String showaward;//配置各地图界面展示的物品

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(mapId);
        buff.writeInt(levelLimit);
        buff.writeInt(itemNum);
        buff.writeInt(rebornTimes);
        buff.writeString(stageitemposition);
        buff.writeString(mapReward);
        buff.writeString(showaward);
    }

    public int getStageIdByIndex(int stageIndex) {
        String[] tmpArr = this.searchStages.split("\\+");
        if (stageIndex < 0 || stageIndex >= tmpArr.length) {
            return -1;
        }
        return Integer.parseInt(tmpArr[stageIndex]);
    }

    /**
     * 根据给定的层次id获取下一个层次索引,如果已经是最后一层,那么返回当前层;
     *
     * @param stageIndex
     * @return
     */
    public Integer getNextStageIndex(int stageIndex) {
        String[] tmpArr = this.searchStages.split("\\+");
        if (stageIndex < 0) {
            return 0;
        }
        return stageIndex >= (tmpArr.length - 1) ? stageIndex : stageIndex + 1;
    }


    public void setSearchStages(String searchStages) {
        this.searchStages = searchStages;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchStages() {
        return searchStages;
    }

    public String getMapImg() {
        return mapImg;
    }

    public void setMapImg(String mapImg) {
        this.mapImg = mapImg;
    }

    public String getMapReward() {
        return mapReward;
    }

    public void setMapReward(String mapReward) {
        this.mapReward = mapReward;
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelLimit(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public String getItemFunction() {
        return itemFunction;
    }

    public void setItemFunction(String itemFunction) {
        this.itemFunction = itemFunction;
    }

    public int getRebornTimes() {
        return rebornTimes;
    }

    public void setRebornTimes(int rebornTimes) {
        this.rebornTimes = rebornTimes;
    }

    public String getStageitemposition() {
        return stageitemposition;
    }

    public void setStageitemposition(String stageitemposition) {
        this.stageitemposition = stageitemposition;
    }

    public String getShowaward() {
        return showaward;
    }

    public void setShowaward(String showaward) {
        this.showaward = showaward;
    }
}
