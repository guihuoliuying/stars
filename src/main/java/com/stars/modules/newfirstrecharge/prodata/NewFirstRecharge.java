package com.stars.modules.newfirstrecharge.prodata;

import com.stars.modules.newfirstrecharge.pojo.NewFirstRechargeReward;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRecharge implements Comparable<NewFirstRecharge> {
    private int id;
    private String awardshow;// '下方奖励展示',
    private int day;// '对应第X天',
    private String pic;// '左边原图展示',
    private String desc;// '描述内容',
    private String award;// '领取的奖励',
    private int activitytype;//'活动类型',
    private int paycount;// '充值额度需求',
    private List<List<NewFirstRechargeReward>> rechargeRewardGroups = new ArrayList<>();
    private List<NewFirstRechargeReward> rechargeRewardShowGroups = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAwardshow() {
        return awardshow;
    }

    public void setAwardshow(String awardshow) {
        this.awardshow = awardshow;
        rechargeRewardShowGroups = new ArrayList<>();
        String[] group = awardshow.split(";");
        for (String item : group) {
            NewFirstRechargeReward newFirstRechargeReward = NewFirstRechargeReward.parse(item);
            rechargeRewardShowGroups.add(newFirstRechargeReward);
        }
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        rechargeRewardGroups = new ArrayList<>();
        String[] awardGroup = award.split("&");
        for (String awardTmp : awardGroup) {
            List<NewFirstRechargeReward> rechargeRewards = new ArrayList<>();
            String[] group = awardTmp.split(";");
            for (String item : group) {
                NewFirstRechargeReward newFirstRechargeReward = NewFirstRechargeReward.parse(item);
                rechargeRewards.add(newFirstRechargeReward);
            }
            rechargeRewardGroups.add(rechargeRewards);
        }

    }

    public NewFirstRechargeReward getAward(int group, int vipLevel) {
        List<NewFirstRechargeReward> rechargeRewards = rechargeRewardGroups.get(group);
        for (NewFirstRechargeReward newFirstRechargeReward : rechargeRewards) {
            if (newFirstRechargeReward.belong(vipLevel)) {
                return newFirstRechargeReward;
            }
        }
        return null;
    }

    public NewFirstRechargeReward getAwardShow(int vipLevel) {
        for (NewFirstRechargeReward newFirstRechargeReward : rechargeRewardShowGroups) {
            if (newFirstRechargeReward.belong(vipLevel)) {
                return newFirstRechargeReward;
            }
        }
        return null;
    }

    public int getActivitytype() {
        return activitytype;
    }

    public void setActivitytype(int activitytype) {
        this.activitytype = activitytype;
    }

    public int getPaycount() {
        return paycount;
    }

    public void setPaycount(int paycount) {
        this.paycount = paycount;
    }

    @Override
    public int compareTo(NewFirstRecharge o) {
        return this.getDay() - o.getDay();
    }

    public void writeBuff(NewByteBuffer buff, int vipLevel) {
        buff.writeInt(id);
        NewFirstRechargeReward awardShow = getAwardShow(vipLevel);
        buff.writeString(awardShow.getRewardText()); // '下方奖励展示',后续变更，奖励随vip等级不同显示不同
        buff.writeInt(day);// '对应第X天',
        buff.writeString(pic);// '左边原图展示',
        buff.writeString(desc);// '描述内容',
        // '领取的奖励',
        buff.writeInt(rechargeRewardGroups.size());
        for (int group = 0; group < rechargeRewardGroups.size(); group++) {
            NewFirstRechargeReward award1 = getAward(group, vipLevel);
            buff.writeInt(award1.getReward().size());
            for (Map.Entry<Integer, Integer> entry : award1.getReward().entrySet()) {
                buff.writeInt(entry.getKey());//itemid
                buff.writeInt(entry.getValue());//count
            }
        }
        buff.writeInt(paycount);// '充值额度需求',
    }
}
