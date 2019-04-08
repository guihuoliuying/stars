package com.stars.modules.teampvpgame.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/15.
 */
public class DoublePVPRewardVo {
    private int rewardType;// 不同阶段奖励类型
    private String rank;// 名次区间
    private String rewardItem;// 奖励
    private int emailTemplate;// 奖励邮件模板Id
    private String showItem;// 显示奖励

    /* 内存数据 */
    private int[] rankArray;// 名次区间
    private Map<Integer, Integer> rewardMap = new HashMap<>();

    public int[] getRankArray() {
        return rankArray;
    }

    public Map<Integer, Integer> getRewardMap() {
        return rewardMap;
    }

    public int getRewardType() {
        return rewardType;
    }

    public void setRewardType(int rewardType) {
        this.rewardType = rewardType;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) throws Exception {
        this.rank = rank;
        if (StringUtil.isEmpty(rank) || "0".equals(rank)) {
            throw new IllegalArgumentException("doublepvpreward表rank字段配置错误");
        }
        rankArray = StringUtil.toArray(rank, int[].class, '+');
        if (rankArray[0] > rankArray[1]) {
            throw new IllegalArgumentException("doublepvpreward表rank字段配置错误");
        }
    }

    public String getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(String rewardItem) {
        this.rewardItem = rewardItem;
        if (StringUtil.isEmpty(rewardItem) || "0".equals(rewardItem)) {
            return;
        }
        rewardMap = StringUtil.toMap(rewardItem, Integer.class, Integer.class, '+', ',');
    }

    public String getShowItem() {
        return showItem;
    }

    public void setShowItem(String showItem) {
        this.showItem = showItem;
    }

    public int getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(int emailTemplate) {
        this.emailTemplate = emailTemplate;
    }
}
