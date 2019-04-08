package com.stars.modules.familyactivities.expedition.prodata;

/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class FamilyActExpeditionStarAwardVo {

    private int condType;
    private int condCount;
    private int itemId;
    private int itemCount;

    public FamilyActExpeditionStarAwardVo(int condType, int condCount, int itemId, int itemCount) {
        this.condType = condType;
        this.condCount = condCount;
        this.itemId = itemId;
        this.itemCount = itemCount;
    }

    public FamilyActExpeditionStarAwardVo(int[] starAward) {
        this.condType = starAward[0];
        this.condCount = starAward[1];
        this.itemId = starAward[2];
        this.itemCount = starAward[3];
    }

    public int getCondType() {
        return condType;
    }

    public void setCondType(int condType) {
        this.condType = condType;
    }

    public int getCondCount() {
        return condCount;
    }

    public void setCondCount(int condCount) {
        this.condCount = condCount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
