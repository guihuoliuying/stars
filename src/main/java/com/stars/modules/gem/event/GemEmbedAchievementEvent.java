package com.stars.modules.gem.event;

import com.stars.core.event.Event;
import com.stars.modules.gem.prodata.GemLevelVo;

import java.util.List;

/**
 * 宝石镶嵌事件;(发送当前类型宝石镶嵌的数量)
 * Created by panzhenfeng on 2016/10/20.
 */
public class GemEmbedAchievementEvent extends Event {
    private byte gemType;
    private List<GemLevelVo> gemLevelVoList;

    public GemEmbedAchievementEvent(byte gemType, List<GemLevelVo> gemLevelVoList) {
        this.gemType = gemType;
        this.gemLevelVoList = gemLevelVoList;
    }


    public byte getGemType() {
        return gemType;
    }

    public List<GemLevelVo> getGemLevelVoList() {
        return gemLevelVoList;
    }
}
