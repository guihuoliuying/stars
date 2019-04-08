package com.stars.services.family.event;

import com.stars.core.dao.DbRowDao;
import com.stars.services.family.event.userdata.FamilyEventPo;

import java.util.List;

import static com.stars.modules.family.FamilyManager.donateListLimit;
import static com.stars.modules.family.FamilyManager.eventListLimit;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyEventData {

    private List<FamilyEventPo> donateList; // 捐献列表
    private List<FamilyEventPo> eventList; // 事件列表

    public FamilyEventData() {
    }

    public FamilyEventData(List<FamilyEventPo> donateList, List<FamilyEventPo> eventList) {
        this.donateList = donateList;
        this.eventList = eventList;
    }

    private void evict(List<FamilyEventPo> list, DbRowDao dao) {
        dao.delete(list.remove(0));
    }

    public void addEvent(FamilyEventPo eventPo, DbRowDao dao) {
        if (eventPo.getEvent() == FamilyEvent.W_RMB_DONATE) {
            if (donateList.size() >= donateListLimit) {
                evict(donateList, dao);
            }
            donateList.add(eventPo);
        } else {
            if (eventList.size() >= eventListLimit) {
                evict(eventList, dao);
            }
            eventList.add(eventPo);
        }
    }

    public List<FamilyEventPo> getEventList() {
        return eventList;
    }

    public void setEventList(List<FamilyEventPo> eventList) {
        this.eventList = eventList;
    }

    public List<FamilyEventPo> getDonateList() {
        return donateList;
    }

    public void setDonateList(List<FamilyEventPo> donateList) {
        this.donateList = donateList;
    }
}
