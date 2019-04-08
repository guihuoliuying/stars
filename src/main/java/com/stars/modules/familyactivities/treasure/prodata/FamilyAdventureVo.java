package com.stars.modules.familyactivities.treasure.prodata;

/**
 * 家族探宝表
 * Created by chenkeyu on 2017/2/10 11:33
 */
public class FamilyAdventureVo {
    private int advLevel;       //等级	    填正整数，表示等级层次，界面显示为第几重
    private int step;           //步数	    填正整数，表示顺序，从小到大顺序挑战
    private String image;       //底图	    填图片资源名，表示界面探宝路线背景图标
    private int stageMonsterId; //boss	    填stagemonsterid，表示指定的怪物，用于获取头像、名字、生命
    private int stageId;        //关卡	    填stageid，表示对应的关卡。
    private String describe;    //描述	    填textid，表示boss描述
    private String showAward;   //奖励展示	填drop表groupid，前端显示，用于展示奖励
    private int awardGroup;     //奖励组	    填familyadvaward表的group，表示关卡根据伤害结算奖励
    private int killAward;      //奖励	    填drop表groupid，表示造成致命一击时的额外奖励
    private int moneyAward;     //家族资金	填整数，表示boss挑战完成后增加家族资金
    private int condition;      //进阶条件	填家族等级，表示家族等级需要达到指定值，才能开启对应阶段，无条件填0
    private int coefficient;    //伤害换算   填整数，表示挑战boss结算伤害后，除以此值再扣除boss生命

    public int getAdvLevel() {
        return advLevel;
    }

    public void setAdvLevel(int advLevel) {
        this.advLevel = advLevel;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStageMonsterId() {
        return stageMonsterId;
    }

    public void setStageMonsterId(int stageMonsterId) {
        this.stageMonsterId = stageMonsterId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getShowAward() {
        return showAward;
    }

    public void setShowAward(String showAward) {
        this.showAward = showAward;
    }

    public int getAwardGroup() {
        return awardGroup;
    }

    public void setAwardGroup(int awardGroup) {
        this.awardGroup = awardGroup;
    }

    public int getKillAward() {
        return killAward;
    }

    public void setKillAward(int killAward) {
        this.killAward = killAward;
    }

    public int getMoneyAward() {
        return moneyAward;
    }

    public void setMoneyAward(int moneyAward) {
        this.moneyAward = moneyAward;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }
}
