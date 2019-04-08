package com.stars.modules.gamecave.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoepidian on 2016/9/13.
 */
public class TinyGameData extends DbRow {
    private long roleId;
    private int gameIndex;
    private int gameId;
    private byte isFinish;
    private String randomCards;
    private String choseCards;
    private String reward;  
    private int score;
    private int star;
    private byte isGetCard;  
    private byte isGetReward;
    private int lastRoundId;
    
    //内存数据
    private List<Integer> randomCardIds = new ArrayList<Integer>();
    private List<Integer> choseCardIds = new ArrayList<Integer>();
    private Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();

    public TinyGameData() {
    	
    }
    
    public TinyGameData(long roleId, int gameIndex, int gameId, byte isFinish, String randomCards, String choseCards, String reward,
    		int score, int star, byte isGetCard, byte isGetReward, int lastRoundId) {
        this.roleId = roleId;
        this.gameIndex = gameIndex;
        this.gameId = gameId;
        this.isFinish = isFinish;       
        this.randomCards = randomCards;
        this.choseCards = choseCards;
        this.reward = reward;
        this.score = score;
        this.star = star;
        this.isGetCard = isGetCard;
        this.isGetReward = isGetReward;
        this.lastRoundId = lastRoundId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "tinygamedata", " roleId=" + this.getRoleId() + " and gameIndex=" + this.getGameIndex());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("tinygamedata", " roleid=" + this.getRoleId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGameIndex(){
    	return gameIndex;
    }
    
    public void setGameIndex(int value){
    	this.gameIndex = value;
    }
    
    public int getGameId(){
    	return gameId;
    }
    
    public void setGameId(int value){
    	this.gameId = value;
    }
    
    public byte getIsFinish(){
    	return isFinish;
    }
    
    public void setIsFinish(byte value){
    	this.isFinish = value;
    }
    
    public String getRandomCards(){
    	return randomCards;
    }
    
    public void setRandomCards(String value){
        this.randomCards = value;
        
        if (randomCards == null || randomCards.equals("") || randomCards.equals("0")) {
			return;
		}
		String[] sts = randomCards.split("\\+");
		for(String tmp : sts){
			randomCardIds.add(Integer.parseInt(tmp));
		}
    }
    
    public String getChoseCards(){
    	return choseCards;
    }
    
    public void setChoseCards(String value) {
        this.choseCards = value;
        
        if (choseCards == null || choseCards.equals("") || choseCards.equals("0")) {
			return;
		}
		String[] sts = choseCards.split("\\+");
		for(String tmp : sts){
			choseCardIds.add(Integer.parseInt(tmp));
		}
    }
    
    public String getReward(){
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
    
    public int getScore(){
    	return score;
    }
    
    public void setScore(int value){
    	this.score = value;
    }
    
    public int getStar(){
    	return star;
    }
    
    public void setStar(int value){
    	this.star = value;
    }
    
    public byte getIsGetCard(){
    	return isGetCard;
    }
    
    public void setIsGetCard(byte value){
    	this.isGetCard = value;
    }
    
    public byte getIsGetReward(){
    	return isGetReward;
    }
    
    public void setIsGetReward(byte value){
    	this.isGetReward = value;
    }

    public int getLastRoundId(){
    	return lastRoundId;
    }
    
    public void setLastRoundId(int value){
    	this.lastRoundId = value;
    }
    
    //内存数据
    public List<Integer> getRandomCardIds(){
    	return randomCardIds;
    }
    
    public void setRandomCardIds(List<Integer> value){
    	randomCardIds = value;
    	
    	StringBuffer buffer = new StringBuffer("");
    	int size = randomCardIds.size();
    	for (int i = 0; i < size; i++) {
			int cardId = randomCardIds.get(i);
			if (i > 0) {
				buffer.append("+");
			}
			buffer.append(cardId);
		}
    	
    	randomCards = buffer.toString();
    }
    
    public List<Integer> getChoseCardIds(){
    	return choseCardIds;
    }
    
    public void setChoseCardIds(List<Integer> value){
    	choseCardIds = value;
    	
    	StringBuffer buffer = new StringBuffer("");
    	int size = choseCardIds.size();
    	for (int i = 0; i < size; i++) {
			int cardId = choseCardIds.get(i);
			if (i > 0) {
				buffer.append("+");
			}
			buffer.append(cardId);
		}
    	
    	choseCards = buffer.toString();
    }
    
    public Map<Integer, Integer> getRewardMap(){
    	return rewardMap;
    }
    
    public void setRewardMap(Map<Integer, Integer> value){
    	rewardMap = value;
    	
    	StringBuffer buffer = new StringBuffer("");
        int index = 0;
        for(Map.Entry<Integer,Integer> entry : rewardMap.entrySet()){
            int itemId = entry.getKey();
            int count = entry.getValue();
            if (index > 0) {
            	buffer.append(",");
			}
            buffer.append(itemId);
            buffer.append("+");
            buffer.append(count);
            index ++;
        }
        
        reward = buffer.toString();
    }
    
    public void writeRecordData(NewByteBuffer buff){
    	buff.writeInt(gameId);
    	buff.writeInt(gameIndex);
    	buff.writeInt(score);
    	buff.writeString(reward);
    	short size = (short)(choseCardIds == null ? 0 : choseCardIds.size());
    	buff.writeShort(size);
    	if (size != 0) {
			for (Integer cardId : choseCardIds) {
				buff.writeInt(cardId);
			}
		}
    }
}
