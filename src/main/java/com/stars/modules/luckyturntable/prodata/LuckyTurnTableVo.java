package com.stars.modules.luckyturntable.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class LuckyTurnTableVo {
    private int id;
    private String levelRange;
    private String vipLevelRange;
    private int timeRange;
    private String item;
    private int odds;
    private int des;

    private int minLv;
    private int maxLv;
    private int minVipLv;
    private int maxVipLv;
    private int itemId;
    private int count;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(itemId);
        buff.writeInt(count);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public void setLevelRange(String levelRange) {
        this.levelRange = levelRange;
        String[] tmp = levelRange.split("\\+");
        this.minLv = Integer.parseInt(tmp[0]);
        this.maxLv = Integer.parseInt(tmp[1]);
    }

    public String getVipLevelRange() {
        return vipLevelRange;
    }

    public void setVipLevelRange(String vipLevelRange) {
        this.vipLevelRange = vipLevelRange;
        String[] tmp = vipLevelRange.split("\\+");
        this.minVipLv = Integer.parseInt(tmp[0]);
        this.maxVipLv = Integer.parseInt(tmp[1]);
    }

    public int getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(int timeRange) {
        this.timeRange = timeRange;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        String[] tmp = item.split("\\+");
        this.itemId = Integer.parseInt(tmp[0]);
        this.count = Integer.parseInt(tmp[1]);
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public int getDes() {
        return des;
    }

    public void setDes(int des) {
        this.des = des;
    }

    public int getMinLv() {
        return minLv;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public int getMinVipLv() {
        return minVipLv;
    }

    public int getMaxVipLv() {
        return maxVipLv;
    }

    @Override
    public String toString() {
        return "LuckyTurnTableVo{" +
                "id=" + id +
                ", levelRange='" + levelRange + '\'' +
                ", vipLevelRange='" + vipLevelRange + '\'' +
                ", timeRange=" + timeRange +
                ", item='" + item + '\'' +
                ", odds=" + odds +
                ", des=" + des +
                '}';
    }
}
