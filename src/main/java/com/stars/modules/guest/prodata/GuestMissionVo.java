package com.stars.modules.guest.prodata;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestMissionVo {

    private int gueMissionId;  // 任务id
    private String reqLevel;   // 要求等级
    private String name;    // 名字
    private String icon;    // 图标
    private String describe;    // 描述
    private byte quality;   // 品质
    private int odds;       // 权值概率
    private byte reqMember; // 需要门客数量
    private int reqTime;    // 需要时间
    private int reqStar;    // 需要星星数
    private int reserveTime;    // 保留时间
    private String award;   // 奖励
    private String limit;   // 指定门客
    private byte protect;   // 刷新保护

    public int getGueMissionId() {
        return gueMissionId;
    }

    public void setGueMissionId(int gueMissionId) {
        this.gueMissionId = gueMissionId;
    }

    public String getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(String reqLevel) {
        this.reqLevel = reqLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public byte getQuality() {
        return quality;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public byte getReqMember() {
        return reqMember;
    }

    public void setReqMember(byte reqMember) {
        this.reqMember = reqMember;
    }

    public int getReqTime() {
        return reqTime;
    }

    public void setReqTime(int reqTime) {
        this.reqTime = reqTime;
    }

    public int getReqStar() {
        return reqStar;
    }

    public void setReqStar(int reqStar) {
        this.reqStar = reqStar;
    }

    public int getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(int reserveTime) {
        this.reserveTime = reserveTime;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public byte getProtect() {
        return protect;
    }

    public void setProtect(byte protect) {
        this.protect = protect;
    }

    public boolean onLevel(int level) {
        String[] levels = reqLevel.split("[+]");
        if (level >= Integer.valueOf(levels[0]) &&
                level <= Integer.valueOf(levels[1])) {
            return true;
        }
        return false;
    }

    /**
     * 保留多少秒
     * @return
     */
    public int getReserveSecond() {
        return reserveTime * 60 * 60;
    }

    /**
     * 检查门客sort是否符合任务
     * @param sort
     * @return
     */
    public boolean checkSort(byte sort) {
        if (limit.equals("0")) {
            return true;
        }
        String[] sorts = limit.split("[+]");
        for (String s : sorts) {
            if (Byte.valueOf(s) == sort) {
                return true;
            }
        }
        return false;
    }
}
