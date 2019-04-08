package com.stars.modules.collectphone.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class StepOperateAct {
    private int operateacttype;//'对应的运营活动类型',
    private int step;//'步骤序号',
    private int type;// '模板类型',
    private String titledesc;// '标题描述',
    private String correct;// '选项',
    private int reward;// '奖励',

    public int getOperateacttype() {
        return operateacttype;
    }

    public void setOperateacttype(int operateacttype) {
        this.operateacttype = operateacttype;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitledesc() {
        return titledesc;
    }

    public void setTitledesc(String titledesc) {
        this.titledesc = titledesc;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(step);//'步骤序号',
        buff.writeInt(type);// '模板类型',
        buff.writeString(titledesc);// '标题描述',
        buff.writeString(correct);// '选项',
        buff.writeInt(reward);// '奖励',
    }
}
