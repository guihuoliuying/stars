package com.stars.modules.marry.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-06.
 */
public class MarrySceneFinishEvent extends Event {
    private byte finish;
    private byte stageType;
    private int stageId;
    private byte status;
    private Map<Integer, Integer> itemMap;
    private int useTime;
    private Map<String, Integer> marryScoreMap;
    private Map<Integer, Integer> extraItemMap;

    public MarrySceneFinishEvent(byte finish, byte stageType, int stageId, byte status, Map<Integer, Integer> itemMap, int useTime, Map<String, Integer> marryScoreMap, Map<Integer, Integer> extraItemMap) {
        this.finish = finish;
        this.stageType = stageType;
        this.stageId = stageId;
        this.status = status;
        this.itemMap = itemMap;
        this.useTime = useTime;
        this.marryScoreMap = marryScoreMap;
        this.extraItemMap = extraItemMap;
    }

    public byte getFinish() {
        return finish;
    }

    public byte getStageType() {
        return stageType;
    }

    public int getStageId() {
        return stageId;
    }

    public byte getStatus() {
        return status;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public int getUseTime() {
        return useTime;
    }

    public Map<String, Integer> getMarryScoreMap() {
        return marryScoreMap;
    }

    public Map<Integer, Integer> getExtraItemMap() {
        return extraItemMap;
    }

    @Override
    public String toString() {
        return "MarrySceneFinishEvent{" +
                "finish=" + finish +
                ", stageType=" + stageType +
                ", stageId=" + stageId +
                ", status=" + status +
                ", itemMap=" + itemMap +
                ", useTime=" + useTime +
                ", marryScoreMap=" + marryScoreMap +
                ", extraItemMap=" + extraItemMap +
                "} " + super.toString();
    }
}
