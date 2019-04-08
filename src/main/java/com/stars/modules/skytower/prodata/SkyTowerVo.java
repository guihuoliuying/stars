package com.stars.modules.skytower.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 镇妖塔的VO数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerVo {
    private int layerId;
    private int layerSerial;
    private int stageid;
    private int timelimit;
    private String firstreward;
    private String dayReward;
    private String challengeSucReward;
    private String challengeFailReward;
    private int levellimit;
    //40+5|30+7|20+9|10+10
    private String jumpLayer;
    private String drama;
    private int fightscore;
    private byte section;
    private int resetlayer;


    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(layerId);
        buff.writeInt(layerSerial);
        buff.writeInt(stageid);
        buff.writeInt(timelimit);
        buff.writeString(firstreward);
        buff.writeString(dayReward);
        buff.writeString(challengeSucReward);
        buff.writeString(challengeFailReward);
        buff.writeInt(levellimit);
        buff.writeString(jumpLayer);
        buff.writeString(drama);
        buff.writeInt(fightscore);
        buff.writeByte(section);
        buff.writeInt(resetlayer);
    }

    public Map<Integer, Integer> getFirstPassRewardsMap(){
        String[] rewardArr = firstreward.split("\\|");
        String[] itemArr = null;
        HashMap<Integer, Integer> rtnMap = new HashMap<>();
        for(int i = 0, len = rewardArr.length; i<len; i++){
            itemArr = rewardArr[i].split("\\+");
            rtnMap.put(Integer.parseInt(itemArr[0]), Integer.parseInt(itemArr[1]));
        }
        return rtnMap;
    }

    public Map<Integer, Integer> getDailyPassRewardsMap(){
        String[] rewardArr = dayReward.split("\\|");
        String[] itemArr = null;
        HashMap<Integer, Integer> rtnMap = new HashMap<>();
        for(int i = 0, len = rewardArr.length; i<len; i++){
            itemArr = rewardArr[i].split("\\+");
            rtnMap.put(Integer.parseInt(itemArr[0]), Integer.parseInt(itemArr[1]));
        }
        return rtnMap;
    }


    public Map<Integer, Integer> getChallengeSucRewardsMap(){
        if(challengeSucReward.equals("0") || !StringUtil.isNotEmpty(challengeSucReward)){
            return null;
        }
        String[] rewardArr = challengeSucReward.split("\\|");
        String[] itemArr = null;
        HashMap<Integer, Integer> rtnMap = new HashMap<>();
        for(int i = 0, len = rewardArr.length; i<len; i++){
            if (!rewardArr[i].equals("0")){
                itemArr = rewardArr[i].split("\\+");
                rtnMap.put(Integer.parseInt(itemArr[0]), Integer.parseInt(itemArr[1]));
            }
        }
        return rtnMap;
    }

    /**
     * 是否是挑战层, 如果有挑战成功奖励才是挑战层;
     * @return
     */
    public boolean isChallengeLayer(){
        if(challengeSucReward.equals("0") || !StringUtil.isNotEmpty(challengeSucReward)){
            return false;
        }
        return true;
    }

    public Map<Integer, Integer> getChallengeFailRewardsMap(){
        if(challengeFailReward.equals("0") || !StringUtil.isNotEmpty(challengeFailReward)){
            return null;
        }
        String[] rewardArr = challengeFailReward.split("\\|");
        String[] itemArr = null;
        HashMap<Integer, Integer> rtnMap = new HashMap<>();
        for(int i = 0, len = rewardArr.length; i<len; i++){
            itemArr = rewardArr[i].split("\\+");
            rtnMap.put(Integer.parseInt(itemArr[0]), Integer.parseInt(itemArr[1]));
        }
        return rtnMap;
    }

    /**
     * 获取可以跳层的增量;
     * @param time
     * @return
     */
    public int getCanJumpAddedLayer(int time){
        if(StringUtil.isNotEmpty(jumpLayer)){
            String[] itemArr = jumpLayer.split("\\|");
            String[] timeJumpArr = null;
            //可以跳到的增量层;
            for(int  i =itemArr.length-1;  i>=0; i--){
                timeJumpArr = itemArr[i].split("\\+");
                if(time <= Integer.parseInt(timeJumpArr[0])){
                    return Integer.parseInt(timeJumpArr[1]);
                }
            }
        }
        return 0;
    }

    public int getLayerId() {
        return layerId;
    }

    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    public int getLayerSerial() {
        return layerSerial;
    }

    public void setLayerSerial(int layerSerial) {
        this.layerSerial = layerSerial;
    }

    public int getTimelimit() {
        return timelimit;
    }

    public void setTimelimit(int timelimit) {
        this.timelimit = timelimit;
    }

    public String getFirstreward() {
        return firstreward;
    }

    public void setFirstreward(String firstreward) {
        this.firstreward = firstreward;
    }

    public String getDayReward() {
        return dayReward;
    }

    public void setDayReward(String dayReward) {
        this.dayReward = dayReward;
    }

    public String getChallengeSucReward() {
        return challengeSucReward;
    }

    public void setChallengeSucReward(String challengeSucReward) {
        this.challengeSucReward = challengeSucReward;
    }

    public String getChallengeFailReward() {
        return challengeFailReward;
    }

    public void setChallengeFailReward(String challengeFailReward) {
        this.challengeFailReward = challengeFailReward;
    }

    public int getLevellimit() {
        return levellimit;
    }

    public void setLevellimit(int levellimit) {
        this.levellimit = levellimit;
    }

    public String getJumpLayer() {
        return jumpLayer;
    }

    public void setJumpLayer(String jumpLayer) {
        this.jumpLayer = jumpLayer;
    }


    public void setStageid(int stageid) {
        this.stageid = stageid;
    }

    public int getStageid(){
        return stageid;
    }

    public String getDrama() {
        return drama;
    }

    public void setDrama(String drama) {
        this.drama = drama;
    }

    public int getFightscore() {
        return fightscore;
    }

    public void setFightscore(int fightscore) {
        this.fightscore = fightscore;
    }

    public byte getSection() {
        return section;
    }

    public void setSection(byte section) {
        this.section = section;
    }

    public int getResetlayer() {
        return resetlayer;
    }

    public void setResetlayer(int resetlayer) {
        this.resetlayer = resetlayer;
    }
}
