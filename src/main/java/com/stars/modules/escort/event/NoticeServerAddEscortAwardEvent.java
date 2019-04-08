package com.stars.modules.escort.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/14.
 */
public class NoticeServerAddEscortAwardEvent extends Event {
    private byte subType;
    private Map<Integer,Integer> award;
    private int carId;
    private byte index;

    public NoticeServerAddEscortAwardEvent(byte subType, Map<Integer, Integer> award, int carId,byte index) {
        this.subType = subType;
        this.award = award;
        this.carId = carId;
        this.index = index;
    }

    public NoticeServerAddEscortAwardEvent(byte subType, Map<Integer, Integer> award) {
        this.subType = subType;
        this.award = award;
    }

    public byte getSubType() {
        return subType;
    }

    public Map<Integer, Integer> getAward() {
        return award;
    }

    public int getCarId() {
        return carId;
    }

    public byte getIndex() {
        return index;
    }
}
