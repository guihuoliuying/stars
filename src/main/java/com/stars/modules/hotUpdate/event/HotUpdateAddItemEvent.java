package com.stars.modules.hotUpdate.event;

import com.stars.core.event.Event;
import com.stars.modules.hotUpdate.HotUpdateConstant;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateAddItemEvent extends Event {
    private int mailTemplateId = HotUpdateConstant.HOTUPDATE_COMPENSATION_MAIL_ID;
    private Map<Integer,Integer> awardMap;
    private byte type = HotUpdateConstant.BY_MAIL;
    private String logSignal = HotUpdateConstant.ADD_LOG_SIGNAL;

    public HotUpdateAddItemEvent(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public HotUpdateAddItemEvent(int mailTemplateId, Map<Integer, Integer> awardMap) {
        this.mailTemplateId = mailTemplateId;
        this.awardMap = awardMap;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public byte getType() {
        return type;
    }

    public String getLogSignal() {
        return logSignal;
    }

    public int getMailTemplateId() {
        return mailTemplateId;
    }
}
