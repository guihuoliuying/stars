package com.stars.modules.newserversign.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

/**
 * Created by gaopeidian on 2016/12/22.
 */
public class NewServerSignVo implements Comparable<NewServerSignVo> {
    private int newServerSignId;
    private int operateActId;
    private int days;
    private int reward;
    private String name;
    private String showPic;
    private String showItem;
    private byte isSpecial;
    private String showEff;
    private String btnPic;
    private String tomorrowShowPic;

    public int getNewServerSignId() {
        return newServerSignId;
    }

    public void setNewServerSignId(int value) {
        this.newServerSignId = value;
    }

    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int value) {
        this.operateActId = value;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int value) {
        this.days = value;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int value) {
        this.reward = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getShowPic() {
        return showPic;
    }

    public void setShowPic(String value) {
        this.showPic = value;
    }

    public String getShowItem() {
        return showItem;
    }

    public void setShowItem(String value) {
        this.showItem = value;
    }

    public byte getIsSpecial() {
        return isSpecial;
    }

    public void setIsSpecial(byte value) {
        this.isSpecial = value;
    }

    public String getTomorrowShowPic() {
        return tomorrowShowPic;
    }

    public void setTomorrowShowPic(String tomorrowShowPic) {
        this.tomorrowShowPic = tomorrowShowPic;
    }

    public String getShowEff() {
        return showEff;
    }

    public void setShowEff(String showEff) {
        if (StringUtil.isEmpty(showEff) || showEff.equals("0")) {
            this.showEff = "";
        } else {
            this.showEff = showEff;
        }
    }

    public String getBtnPic() {
        return btnPic;
    }

    public void setBtnPic(String btnPic) {
        this.btnPic = btnPic;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(newServerSignId); //奖励id
        buff.writeInt(days);            //天数
        buff.writeString(name);         //名字
        buff.writeString(showPic);      //图片
        buff.writeString(showItem);     //显示奖励
        buff.writeString(showEff);
        buff.writeByte(isSpecial);      //是否是特殊奖励
        buff.writeString(btnPic);
        buff.writeString(tomorrowShowPic);//明日展示图片
    }

    /**
     * 按活动days从小到大排
     */
    @Override
    public int compareTo(NewServerSignVo o) {
        if (this.getDays() < o.getDays()) {
            return -1;
        } else if (this.getDays() > o.getDays()) {
            return 1;
        } else {
            return 0;
        }
    }
}
