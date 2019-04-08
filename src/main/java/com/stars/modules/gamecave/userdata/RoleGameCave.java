package com.stars.modules.gamecave.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/9/13.
 */
public class RoleGameCave extends DbRow {
    private long roleId;
    private int score;
    private String choseCards;
    private String reward;
    private byte isGetReward;
    
    //内存数据
    List<Integer> choseCardIds = new ArrayList<Integer>();
    Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();
    
    public RoleGameCave() {
    	
    }
    
    public RoleGameCave(long roleId, int score, String choseCards, String reward, byte isGetReward){
    	this.roleId = roleId;
    	this.score = score;
    	this.choseCards = choseCards;
    	this.reward = reward;
    	this.isGetReward = isGetReward;
    }
    
    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolegamecave", " roleid=" + this.getRoleId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolegamecave", " roleid=" + this.getRoleId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
    
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public String getChoseCards() {
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
    
    public byte getIsGetReward() {
        return isGetReward;
    }

    public void setIsGetReward(byte value) {
        this.isGetReward = value;
    }
    
    
    //内存数据
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
    	buff.writeByte(isGetReward);
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
