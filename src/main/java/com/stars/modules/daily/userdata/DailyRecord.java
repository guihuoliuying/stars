package com.stars.modules.daily.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DailyRecord extends DbRow {
	
	private long roleId;
	
	private Map<Short, Integer>recordMap;

	private int dayFightScore;

	private int dayRoleLevel;

	private int superAwardDailyId;

	private int superAwardId;

	private byte hadDrawToday; //今日是否已经抽过签


	private int dailyBallLevel; //斗魂珠等级

	
	/**
	 * 已领取的奖励
	 */
	private HashSet<Integer> gotAwardSet;
	private HashSet<Integer> canGetAwardSet = new HashSet<>(); //可以领取的奖励
	
	public DailyRecord(){}
	
	public DailyRecord(long roleId){
		this.roleId = roleId;
		this.recordMap = new HashMap<Short, Integer>();
		this.gotAwardSet = new HashSet<Integer>();
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "dailyrecord",
        		" roleid=" + this.roleId);
	}

	@Override
	public String getDeleteSql() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRecord(String record){
		recordMap = new HashMap<Short, Integer>();
		if (record.equals("")) {
			return;
		}
		String[] strings = record.split("[|]");
		for (String string : strings) {
			String[] ss = string.split("[=]");
			recordMap.put(Short.parseShort(ss[0]),Integer.parseInt(ss[1]));
		}
	}
	
	public String getRecord(){
		if (recordMap == null || recordMap.size() <= 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		Set<Short>set = recordMap.keySet();
		for (Short dailyId : set) {
			if (buffer.length() > 0) {
				buffer.append("|");
			}
			buffer.append(dailyId).append("=");
			buffer.append(recordMap.get(dailyId));
		}
		return buffer.toString();
	}

	
	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public Map<Short, Integer> getRecordMap() {
		return recordMap;
	}

	public void setRecordMap(Map<Short, Integer> recordMap) {
		this.recordMap = recordMap;
	}
	
	public int getDailyRecord(short dailyId){
		if (recordMap.containsKey(dailyId)) {
			return recordMap.get(dailyId);
		}
		return 0;
	}
	
	public void putDailRecord(short dailyId,int count){
		this.recordMap.put(dailyId, count);
	}

	public String getGotAward() {
		if (gotAwardSet.size() <= 0) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		for (int dex:gotAwardSet) {
			if (buff.length() > 0) {
				buff.append("&");
			}
			buff.append(dex);
		}
		return buff.toString();
	}

	public void setGotAward(String string) {
		this.gotAwardSet = new HashSet<Integer>();
		if (string.equals("")) {
			return;
		}
		String string2[] = string.split("&");
		for (String string3 : string2) {
			gotAwardSet.add(Integer.parseInt(string3));
		}
	}

	public void setCanGetAward(String canGetAward){
		this.canGetAwardSet = new HashSet<>();
		if(StringUtil.isEmpty(canGetAward)){
			return;
		}
		String[] array = canGetAward.split("&");
		for(String awrdIdStr:array){
			canGetAwardSet.add(Integer.parseInt(awrdIdStr));
		}
	}

	public String getCanGetAward(){
		if (canGetAwardSet.size() <= 0) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		for (int awardId:canGetAwardSet) {
			if (buff.length() > 0) {
				buff.append("&");
			}
			buff.append(awardId);
		}
		return buff.toString();
	}

	
	public boolean isGotAward(int award){
		return this.gotAwardSet.contains(award);
	}
	
	public void gotAward(int award){
		this.gotAwardSet.add(award);
	}
	
	public void clearGotAward(){
		this.gotAwardSet.clear();
	}

	public void clearCanGetAward(){
		this.canGetAwardSet.clear();
	}

	public HashSet<Integer> getGotAwardSet() {
		return gotAwardSet;
	}

	public void canGetAward(int awardId){
		this.canGetAwardSet.add(awardId);
	}

	public HashSet<Integer> getCanGetAwardSet() {
		return canGetAwardSet;
	}

	public void setCanGetAwardSet(HashSet<Integer> canGetAwardSet) {
		this.canGetAwardSet = canGetAwardSet;
	}

	public int getDayFightScore() {
		return dayFightScore;
	}

	public void setDayFightScore(int dayFightScore) {
		this.dayFightScore = dayFightScore;
	}

	public boolean isTriggerSuperAwardToday(){
		return superAwardDailyId > 0;  //有触发才会记录战力差
	}

	public int getDayRoleLevel() {
		return dayRoleLevel;
	}

	public void setDayRoleLevel(int dayRoleLevel) {
		this.dayRoleLevel = dayRoleLevel;
	}

	public byte getHadDrawToday() {
		return hadDrawToday;
	}

	public void setHadDrawToday(byte hadDrawToday) {
		this.hadDrawToday = hadDrawToday;
	}

	public int getDailyBallLevel() {
		return dailyBallLevel;
	}

	public void setDailyBallLevel(int dailyBallLevel) {
		this.dailyBallLevel = dailyBallLevel;
	}

	public int getSuperAwardDailyId() {
		return superAwardDailyId;
	}

	public void setSuperAwardDailyId(int superAwardDailyId) {
		this.superAwardDailyId = superAwardDailyId;
	}

	public int getSuperAwardId() {
		return superAwardId;
	}

	public void setSuperAwardId(int superAwardId) {
		this.superAwardId = superAwardId;
	}
}
