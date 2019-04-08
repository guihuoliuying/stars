package com.stars.modules.newofflinepvp.prodata;

/**
 * Created by chenkeyu on 2017-03-08 16:17
 */
public class OfflineMatchVo {
    private String rankSection;
    private int minMatch;
    private int maxMatch;

    //内存数据
    private int minRank;
    private int maxRank;

    public int getMinRank() {
        return minRank;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public String getRankSection() {
        return rankSection;
    }

    public void setRankSection(String rankSection) {
        this.rankSection = rankSection;
        String[] tmpStr = rankSection.split("\\+");
        this.minRank = Integer.parseInt(tmpStr[0]);
        this.maxRank = Integer.parseInt(tmpStr[1]);
    }

    public int getMinMatch() {
        return minMatch;
    }

    public void setMinMatch(int minMatch) {
        this.minMatch = minMatch;
    }

    public int getMaxMatch() {
        return maxMatch;
    }

    public void setMaxMatch(int maxMatch) {
        this.maxMatch = maxMatch;
    }
}
