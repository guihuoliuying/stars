package com.stars.services.marry.event;

import com.stars.core.event.Event;

/**
 * Created by zhoujin on 2017/4/27.
 */
public class MarryLogEvent extends Event {
    private String operateId;
    private String staticStr;
    private String target;
    public MarryLogEvent(String operateId,String staticStr,String target) {
        this.operateId = operateId;
        this.staticStr = staticStr;
        this.target = target;
    }
    public String getOperateId() {
        return this.operateId;
    }

    public String getStaticStr() {
        return this.staticStr;
    }

    public String getTarget() {
        return this.target;
    }
}
