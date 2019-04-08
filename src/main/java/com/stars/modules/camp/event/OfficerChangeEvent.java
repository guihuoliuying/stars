package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/7/10.
 */
public class OfficerChangeEvent extends Event {
    private int commonOfficerId;
    public int rareOfficerId;
    private int designateOfficerId;

    public int getCommonOfficerId() {
        return commonOfficerId;
    }

    public void setCommonOfficerId(int commonOfficerId) {
        this.commonOfficerId = commonOfficerId;
    }

    public int getRareOfficerId() {
        return rareOfficerId;
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public int getDesignateOfficerId() {
        return designateOfficerId;
    }

    public void setDesignateOfficerId(int designateOfficerId) {
        this.designateOfficerId = designateOfficerId;
    }
}
