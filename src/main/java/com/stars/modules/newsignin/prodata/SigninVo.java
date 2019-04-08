package com.stars.modules.newsignin.prodata;

import com.stars.modules.newsignin.NewSigninConst;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017/2/5 17:11
 */
public class SigninVo {
    private int signinId;           //签到id, 签到奖励唯一标识
    private String param;           //签到参数,和type挂钩,type=1,参数为日期,格式为yy-mm-dd,type=2, yy-mm+次数+图标名称,type=3,yy-mm+次数+图片名称
    private int type;               //签到类型. 1=单日签到, 2=累积奖励,3=特殊奖励
    private int reward;             //签到奖励, dropGroup
    private String showItem;        //显示用的奖励物品,格式为itemid+数量
    private String vipBenefit;      //vip领取的倍率,格式为vip等级+倍率+倍率文本索引.倍率最小为2
    private String img;             //字符串, 界面宣传图资源名称. 只针对type=3的签到数据有效
    private String weekendbenefit;  //周x领取的倍率。格式为：倍率x+倍率文本索引gametext，倍率最小为2该字段的优先级高于vipbenefit。如果两个字段都有内容，只有weekendbenefit生效


    public SigninVo() {
    }

    //内存数据
    //param
    private String yyyymmdd;
    private String yyyymm;
    private int count;
    //vipbenefit
    private int vipLv;
    private int benefit;

    private int benefit0;//周x翻倍，优先级比benefit高

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(signinId);
        buff.writeString(param);
        buff.writeInt(type);
        buff.writeInt(reward);
        buff.writeString(showItem);
        buff.writeString(vipBenefit);
        buff.writeString(img);
        buff.writeString(weekendbenefit);
    }

    public int getSigninId() {
        return signinId;
    }

    public void setSigninId(int signinId) {
        this.signinId = signinId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
        if (this.type == NewSigninConst.singleSign) {
            this.yyyymmdd = param;
        }
        if (this.type == NewSigninConst.accumulateAward || this.type == NewSigninConst.specialAward) {
            String[] paramStr = param.split("\\+");
            this.yyyymm = paramStr[0];
            this.count = Integer.parseInt(paramStr[1]);
        }
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public String getShowItem() {
        return showItem;
    }

    public void setShowItem(String showItem) {
        this.showItem = showItem;
    }

    public String getVipBenefit() {
        return vipBenefit;
    }

    public void setVipBenefit(String vipBenefit) {
        this.vipBenefit = vipBenefit;
        if (!vipBenefit.equals("0")) {
            String[] vipbenefitStr = vipBenefit.split("\\+");
            this.vipLv = Integer.parseInt(vipbenefitStr[0]);
            this.benefit = Integer.parseInt(vipbenefitStr[1]);
        } else {
            this.vipLv = 0;
            this.benefit = 1;
        }
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setYyyymmdd(String yyyymmdd) {
        this.yyyymmdd = yyyymmdd;
    }

    public void setYyyymm(String yyyymm) {
        this.yyyymm = yyyymm;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getYyyymmdd() {
        return yyyymmdd;
    }

    public String getYyyymm() {
        return yyyymm;
    }

    public int getCount() {
        return count;
    }

    public int getVipLv() {
        return vipLv;
    }

    public int getBenefit() {
        return benefit;
    }

    public int getBenefit0() {
        return benefit0;
    }

    public String getWeekendbenefit() {
        return weekendbenefit;
    }

    public void setWeekendbenefit(String weekendbenefit) {
        this.weekendbenefit = weekendbenefit;
        if (!weekendbenefit.equals("0")) {
            String[] weekendbenefitStr = weekendbenefit.split("\\+");
            this.benefit0 = Integer.parseInt(weekendbenefitStr[0]);
        } else {
            this.benefit0 = 1;
        }
    }
}
