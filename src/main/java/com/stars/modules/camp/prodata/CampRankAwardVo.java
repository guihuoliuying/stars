package com.stars.modules.camp.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampRankAwardVo {
    private Integer type;//对应城池表的id
    private String section;//填低名次+高名次，表示名次段，高低相同表示对应名次
    private String showaward;//填道具id=数量，表示奖励道具及数量，多个用|隔开，纯展示用
    private String award;//填道具id=数量，表示奖励道具及数量，多个用|隔开
    private Integer rareoffice;//配置rareofficer表的id，表示获得该名次时可获得该官职
    private String moment;//填1|21:00:00表示每天21点发放，2|7|21:00:00表示每周日21点发放
    private Integer email;//填emailtemplate表templateid，表示发放奖励的邮件标题和内容。
    private int lowSection;
    private int highSectin;
    private Map<Integer, Integer> raward;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
        try {
            Integer[] group = StringUtil.toArray(section, Integer[].class, '+');
            lowSection = group[0];
            highSectin = group[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getShowaward() {
        return showaward;
    }

    public void setShowaward(String showaward) {
        this.showaward = showaward;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        raward = StringUtil.toMap(award, Integer.class, Integer.class, '=', '|');
    }

    public Integer getRareoffice() {
        return rareoffice;
    }

    public void setRareoffice(Integer rareoffice) {
        this.rareoffice = rareoffice;
    }

    public String getMoment() {
        return moment;
    }

    public void setMoment(String moment) {
        this.moment = moment;
    }

    public Integer getEmail() {
        return email;
    }

    public void setEmail(Integer email) {
        this.email = email;
    }

    public Map<Integer, Integer> getRaward() {
        return raward;
    }

    public boolean isInThisSection(int rank) {
        if (rank >= lowSection && rank <= highSectin) {
            return true;
        }
        return false;
    }
}
