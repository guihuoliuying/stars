package com.stars.modules.email.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by panzhenfeng on 2016/10/28.
 */
public class RequestSendSingleEmailEvent extends Event {
    private int curstomType;
    private long receiveRoleId;
    private int templateId;
    private long sendId;
    private String sendName;
    private Map<Integer, Integer> affixMap ;

    public RequestSendSingleEmailEvent(long receiveRoleId, int templateId, long sendId, String sendName, Map<Integer, Integer> affixMap){
        this.receiveRoleId = receiveRoleId;
        this.templateId = templateId;
        this.sendId = sendId;
        this.sendName = sendName;
        this.affixMap = affixMap;
    }

    public long getReceiveRoleId() {
        return receiveRoleId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public long getSendId() {
        return sendId;
    }

    public String getSendName() {
        return sendName;
    }

    public Map<Integer, Integer> getAffixMap() {
        return affixMap;
    }

    public int getCurstomType() {
        return curstomType;
    }

    public void setCurstomType(int curstomType) {
        this.curstomType = curstomType;
    }
}
