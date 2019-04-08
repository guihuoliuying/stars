package com.stars.modules.familyactivities.treasure.prodata;

/**
 * 家族探宝周日关卡表
 * Created by chenkeyu on 2017/2/10 11:33
 */
public class FamilyAdvendVo {
    private String endCondition;    //条件	     填阶级+step，表示周日0点时，根据当前进度生成宝箱关卡
    private int stageId;            //关卡	     填stageid，表示宝箱关卡
    private String image;           //宝箱图标	 填美术图片资源名，表示宝箱关卡图标
    private String name;            //名字	     填textid，表示周日宝藏的名字
    private String describe;        //说明	     填textid，表示界面说明文本
    private String endShowAward;    //奖励展示	 填drop表groupid，前端显示，用于展示奖励
    private int award;              //奖励	     填drop表groupid，用于后端结算奖励，表示周日关卡结算奖励

    public String getEndCondition() {
        return endCondition;
    }

    public void setEndCondition(String endCondition) {
        this.endCondition = endCondition;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public String getEndShowAward() {
        return endShowAward;
    }

    public void setEndShowAward(String endShowAward) {
        this.endShowAward = endShowAward;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }
}
