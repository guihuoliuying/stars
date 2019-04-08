package com.stars.modules.familyTask.prodata;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyMissionGroup {
    private int id;
    private int groupId;
    private String reqItem;
    private int reqCode;
    private int reqCount;
    private int odds;
    private byte help;
    private int award;
    private int helpaward;
    private int goldAward;
    private int reqGold;
    private int rank;
    private String helpdesc;
    private int babySweepId;//宝宝扫荡id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
        String[] array = reqItem.split("\\+");
        this.reqCode = Integer.parseInt(array[0]);
        this.reqCount = Integer.parseInt(array[1]);
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public byte getHelp() {
        return help;
    }

    public void setHelp(byte help) {
        this.help = help;
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getHelpaward() {
        return helpaward;
    }

    public void setHelpaward(int helpaward) {
        this.helpaward = helpaward;
    }

    public int getGoldAward() {
        return goldAward;
    }

    public void setGoldAward(int goldAward) {
        this.goldAward = goldAward;
    }

    public int getReqGold() {
        return reqGold;
    }

    public void setReqGold(int reqGold) {
        this.reqGold = reqGold;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getHelpdesc() {
        return helpdesc;
    }

    public void setHelpdesc(String helpdesc) {
        this.helpdesc = helpdesc;
    }

    public int getReqCount() {
        return reqCount;
    }

    public int getReqCode() {
        return reqCode;
    }

    public int getBabySweepId() {
        return babySweepId;
    }

    public void setBabySweepId(int babySweepId) {
        this.babySweepId = babySweepId;
    }
}
