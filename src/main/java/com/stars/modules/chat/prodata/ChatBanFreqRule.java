package com.stars.modules.chat.prodata;

/**
 * Created by zhanghaizhen on 2017/5/10.
 */
public class ChatBanFreqRule {
    private  short secondInterval; //检测的时间间隔
    private short freqLimit;  //检测的发言频率阀值
    private int banSeconds; //需要被禁言的时间
    
    public ChatBanFreqRule(short secondInterval,short freqLimit,short banSeconds){
        this.secondInterval = secondInterval;
        this.freqLimit = freqLimit;
        this.banSeconds = banSeconds;
    }

    public short getSecondInterval() {
        return secondInterval;
    }

    public void setSecondInterval(short secondInterval) {
        this.secondInterval = secondInterval;
    }

    public short getFreqLimit() {
        return freqLimit;
    }

    public void setFreqLimit(short freqLimit) {
        this.freqLimit = freqLimit;
    }

    public int getBanSeconds() {
        return banSeconds;
    }

    public void setBanSeconds(int banSeconds) {
        this.banSeconds = banSeconds;
    }

    public String toString(){
        return new String(secondInterval + "+" + freqLimit + "+" + banSeconds);
    }
}
