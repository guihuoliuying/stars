package com.stars.modules.chat.prodata;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhanghaizhen on 2017/5/9.
 */

public class ChatBanVo {
    //数据库
    private byte channel;  //频道
    private String  levelSection; //等级段
    private String vipSection; //VIP等级段
    private int wordLimit; //发言长度
    private int timeLimit; //发言间隔
    private String generalBan; //禁言规则
    private String silentBan; //静默禁言规则

    //内存
    private int minLv; //最小等级
    private int maxLv; //最大等级
    private int minVipLv; //最小vip等级
    private int maxVipLv; //最大vip等级
    private short maxFreqSecond; //最大的时间间隔检测
    private List<ChatBanFreqRule> FreqRuleList = new ArrayList<ChatBanFreqRule>(); //玩家聊天频率限制
    private Map silentRuleMap = new TreeMap(); //玩家重复发言最高次数

    public int getMinLv() {
        return minLv;
    }

    public void setMinLv(int minLv) {
        this.minLv = minLv;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }

    public int getMinVipLv() {
        return minVipLv;
    }

    public void setMinVipLv(int minVipLv) {
        this.minVipLv = minVipLv;
    }

    public int getMaxVipLv() {
        return maxVipLv;
    }

    public void setMaxVipLv(int maxVipLv) {
        this.maxVipLv = maxVipLv;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public String getLevelSection() {
        return levelSection;
    }

    public void setLevelSection(String levelSection) throws Exception {
        this.levelSection = levelSection;
        int[] levelSectionArray = StringUtil.toArray(levelSection, int[].class, '+');
        minLv = levelSectionArray[0];
        maxLv = levelSectionArray[1];
    }

    public String getVipSection() {
        return vipSection;
    }

    public void setVipSection(String vipSection) throws Exception {
        this.vipSection = vipSection;
        int[] vipLevelSectionArray = StringUtil.toArray(vipSection,int[].class,'+');
        minVipLv = vipLevelSectionArray[0];
        maxVipLv = vipLevelSectionArray[1];
    }

    public int getWordLimit() {
        return wordLimit;
    }

    public void setWordLimit(int wordLimit) {
        this.wordLimit = wordLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getGeneralBan() {
        return generalBan;
    }


    /**
     * 格式为：300+20+1800|600+39+7200，表示5分钟不足20条，但10分钟发了39条，则禁言2小时
     * @param generalBan
     * @throws Exception
     */
    public void setGeneralBan(String generalBan) throws Exception {
        this.generalBan = generalBan;
        String[] banRuleArray = StringUtil.toArray(generalBan,String[].class,'|');
        for (int i = 0; i < banRuleArray.length; i ++){
            short[] args = StringUtil.toArray(banRuleArray[i],short[].class,'+');
            ChatBanFreqRule freqRule =  new ChatBanFreqRule(args[0],args[1],args[2]);
            FreqRuleList.add(freqRule);
            if (args[0] > maxFreqSecond){
                maxFreqSecond = args[0];
            }
        }
    }

    public String getSilentBan() {
        return silentBan;
    }

    public void setSilentBan(String silentBan) throws Exception {
        this.silentBan = silentBan;
        String[] banRuleArray = StringUtil.toArray(silentBan,String[].class,'|');
        for(int i = 0; i < banRuleArray.length; i++){
            short[] args = StringUtil.toArray(banRuleArray[i],short[].class,'+');
            silentRuleMap.put(args[0],args[1]);
        }
    }

    public short getMaxFreqSecond() {
        return maxFreqSecond;
    }

    public List<ChatBanFreqRule> getFreqRuleList() {
        return FreqRuleList;
    }

    public Map getSilentRuleMap() {
        return silentRuleMap;
    }
}
