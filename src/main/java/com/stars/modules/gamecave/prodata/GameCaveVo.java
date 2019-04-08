package com.stars.modules.gamecave.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/1/12.
 */
public class GameCaveVo {
    private int gameId;
    private String name;
    private int npcId;
    private String icon;
    private byte type;
    private int gameTimes;
    private String cardRewardNum;
    private String reward;
    private String scoreNum;
    private String gameCamera;
    
    /* 内存数据 */
    //<星数,<随机出来的牌数，可选牌数>>
    private Map<Integer, List<Integer>> cardRewardNumMap = new HashMap<Integer, List<Integer>>();
    //<星数,奖励dropId>
    private Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();
    private List<Integer> scoreNumList = new ArrayList<Integer>();
    private String starDesc;
   
    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(gameId);
        buff.writeString(name);
        buff.writeInt(npcId);
        buff.writeByte(type);
        buff.writeInt(gameTimes);
        buff.writeString(gameCamera);
        buff.writeString(icon);
        buff.writeString(scoreNum);
    }
    
    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;       	
    }
    
    public int getGameTimes() {
        return gameTimes;
    }

    public void setGameTimes(int value) {
        this.gameTimes = value;
    }
    
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String value) {
        this.icon = value;
    }
    
    public String getCardRewardNum() {
        return cardRewardNum;
    }

    public void setCardRewardNum(String value) {
        this.cardRewardNum = value;
        
        if (cardRewardNum == null || cardRewardNum.equals("") || cardRewardNum.equals("0")) {
			return;
		}
		String[] sts = cardRewardNum.split("\\,");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 3) {
				List<Integer> cardNumList = new ArrayList<Integer>();
				cardNumList.add(Integer.parseInt(ts[1]));
				cardNumList.add(Integer.parseInt(ts[2]));
				cardRewardNumMap.put(Integer.parseInt(ts[0]), cardNumList);
			}
		}
    }
    
    public String getReward() {
        return reward;
    }

    public void setReward(String value) {
        this.reward = value;
        
        if (reward == null || reward.equals("") || reward.equals("0")) {
			return;
		}
		String[] sts = reward.split("\\,");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 2) {
				rewardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
			}
		}
    }
    
    public String getScoreNum() {
        return scoreNum;
    }

    public void setScoreNum(String value) {
        this.scoreNum = value;
        
        if (scoreNum == null || scoreNum.equals("") || scoreNum.equals("0")) {
			return;
		}
		String[] sts = scoreNum.split("\\+");
		int length = sts.length;
		for (int i = 0; i < length; i++) {
			if (i == length - 1) {
				starDesc = sts[i];
			}else{
				scoreNumList.add(Integer.parseInt(sts[i]));
			}
		}
    }
    
    public String getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(String value) {
        this.gameCamera = value;
    }
    
    
    public Map<Integer, List<Integer>> getCardRewardNumMap(){
    	return this.cardRewardNumMap;
    }
    
    public Map<Integer, Integer> getRewardMap(){
    	return this.rewardMap;
    }
    
    public List<Integer> getScoreNumList(){
    	return this.scoreNumList;
    }
    
    public String getStarDesc(){
    	return this.starDesc;
    }
    
    public int getStarBySuccessCount(int successCount){
    	int star = 0;
    	int size = scoreNumList.size();
    	for (int i = 0; i < size; i++) {
			if (successCount < scoreNumList.get(i)) {
			    star = i - 1;
			    break;
			}
		}
    	
    	int max = scoreNumList.get(size - 1);
    	if (successCount >= max) {
			star = size - 1;
		}
    	
    	return star >= 0 ? star : 0;
    }
}
