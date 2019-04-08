package com.stars.modules.familyactivities.expedition.prodata;

/**
 * Created by zhaowenshuo on 2016/10/12.
 */
public class FamilyActExpeditionBuffInfoVo {

    private int id;
    private int level = 1;
    private int reqItemId;
    private int reqItemCount;

    public FamilyActExpeditionBuffInfoVo(int id, int reqItemId, int reqItemCount) {
        this.id = id;
        this.reqItemId = reqItemId;
        this.reqItemCount = reqItemCount;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getReqItemId() {
        return reqItemId;
    }

    public int getReqItemCount() {
        return reqItemCount;
    }
}
