package com.stars.services.escort;

import com.stars.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/21.
 */
public class RobTempCache {
    private String fightId;
    private Map<Long,Escorter> robberMap = new HashMap<>();
    private long createTime;

    public RobTempCache(Map<Long, Escorter> robberMap,String fightId) {
        this.robberMap = robberMap;
        this.createTime = System.currentTimeMillis();
        this.fightId = fightId;
    }

    public Map<Long, Escorter> getRobberMap() {
        return robberMap;
    }

    public boolean isTimeOut(){
        long now = System.currentTimeMillis();
        return now - createTime >= 10 * DateUtil.SECOND;
    }

    public String getFightId() {
        return fightId;
    }
}
