package com.stars.modules.runeDungeon.userData;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;

public class RuneDungeonPo extends DbRow{
	
	private long roleId;//角色id
	
	private int dungeonId;//副本id
	
	private int singleCha;//单人挑战进度
	
	private String teamStageIdStr;
	
	private Map<Integer, List<Integer>> teamStageIdMap;//好友助战挑战关卡列表
	
	private int helpAwardTimes;//每日助战收益次数
	
	private String helpStr;
	
	private Map<Long, Integer> helpMap;//冷却中助战好友
	
	private String teamChaStr;
	
	private Map<Integer, FriendHelpChaInfo> teamChaMap;//助战副本进度信息
	
	private String helpRewardStr;
	
	private Map<Integer, Long> helpReward;//助战奖励
	
	private String haveHelpPlayer;
	
	private Set<Long> haveHelpPlayerSet;//已帮助玩家列表
	
	private long helpRewardUpdateTime;//助战奖励更新时间戳
	
	public RuneDungeonPo() {
		// TODO Auto-generated constructor stub
	}
	
	public RuneDungeonPo(long roleId, int dungeonId) {
		this.roleId = roleId;
		this.dungeonId = dungeonId;
		this.teamStageIdMap = new HashMap<>();
		this.helpMap = new HashMap<>();
		this.teamChaMap = new HashMap<>();
		this.helpReward = new HashMap<>();
		this.haveHelpPlayerSet = new HashSet<>();
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getSingleCha() {
		return singleCha;
	}

	public void setSingleCha(int singleCha) {
		this.singleCha = singleCha;
	}

	public String getTeamStageIdStr() {
		Iterator<Entry<Integer, List<Integer>>> iterator = teamStageIdMap.entrySet().iterator();
		StringBuffer sb = new StringBuffer();
		Entry<Integer, List<Integer>> entry = null;
		for(;iterator.hasNext();){
			entry = iterator.next();
			if(sb.length()==0){
				sb.append(entry.getKey()).append(":").append(StringUtil.makeString2(entry.getValue(), ','));
			}else{
				sb.append("&").append(entry.getKey()).append(":").append(StringUtil.makeString2(entry.getValue(), ','));
			}
		}
		teamStageIdStr = sb.toString();
		return teamStageIdStr;
	}

	public void setTeamStageIdStr(String teamStageIdStr) throws Exception {
		this.teamStageIdStr = teamStageIdStr;
		Map<Integer, List<Integer>> map = new HashMap<>();
		if(StringUtil.isNotEmpty(teamStageIdStr)){			
			String[] strs = teamStageIdStr.split("&");
			for(String str : strs){
				String[] infos = str.split(":");
				List<Integer> list = StringUtil.toArrayList(infos[1], Integer.class, ',');
				map.put(Integer.parseInt(infos[0]), list);
			}
		}
		this.teamStageIdMap = map;
	}

	public Map<Integer, List<Integer>> getTeamStageIdMap() {
		return teamStageIdMap;
	}

	public void setTeamStageIdMap(Map<Integer, List<Integer>> teamStageIdMap) {
		this.teamStageIdMap = teamStageIdMap;
	}

	public int getHelpAwardTimes() {
		return helpAwardTimes;
	}

	public void setHelpAwardTimes(int helpAwardTimes) {
		this.helpAwardTimes = helpAwardTimes;
	}

	public String getHelpStr() {
		helpStr = StringUtil.makeString(helpMap,  '+', '|');
		return helpStr;
	}

	public void setHelpStr(String helpStr) {
		this.helpStr = helpStr;
		this.helpMap = StringUtil.toMap(helpStr, Long.class, Integer.class, '+', '|');
	}

	public Map<Long, Integer> getHelpMap() {
		return helpMap;
	}

	public void setHelpMap(Map<Long, Integer> helpMap) {
		this.helpMap = helpMap;
	}

	public String getTeamChaStr() {
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<Integer, FriendHelpChaInfo>> iterator = teamChaMap.entrySet().iterator();
		Entry<Integer, FriendHelpChaInfo> entry = null;
		FriendHelpChaInfo chaInfo = null;
		for(;iterator.hasNext();){
			entry = iterator.next();
			chaInfo = entry.getValue();
			if(sb.length()==0){
				sb.append(entry.getKey()).append(":").append(chaInfo.getDungeonId()).append(",")
				.append(chaInfo.getChaStep()).append(",").append(chaInfo.getAngerLevel()).append(",")
				.append(chaInfo.getKillRun());
			}else{
				sb.append("&").append(entry.getKey()).append(":").append(chaInfo.getDungeonId()).append(",")
				.append(chaInfo.getChaStep()).append(",").append(chaInfo.getAngerLevel()).append(",")
				.append(chaInfo.getKillRun());
			}
		}
		this.teamChaStr = sb.toString();
		return teamChaStr;
	}

	public void setTeamChaStr(String teamChaStr) {
		this.teamChaStr = teamChaStr;
		Map<Integer, FriendHelpChaInfo> map = new HashMap<Integer, FriendHelpChaInfo>();
		if(StringUtil.isNotEmpty(teamChaStr)){			
			String[] strs = teamChaStr.split("&");
			for(String str : strs){
				String[] infos = str.split(":");
				String[] arr = infos[1].split(",");
				FriendHelpChaInfo friendHelpChaInfo = new FriendHelpChaInfo();
				friendHelpChaInfo.setDungeonId(Integer.parseInt(arr[0]));
				friendHelpChaInfo.setChaStep(Integer.parseInt(arr[1]));
				friendHelpChaInfo.setAngerLevel(Integer.parseInt(arr[2]));
				friendHelpChaInfo.setKillRun(Integer.parseInt(arr[3]));
				map.put(Integer.parseInt(infos[0]), friendHelpChaInfo);
			}
		}
		teamChaMap = map;
	}

	public Map<Integer, FriendHelpChaInfo> getTeamChaMap() {
		return teamChaMap;
	}

	public void setTeamChaMap(Map<Integer, FriendHelpChaInfo> teamChaMap) {
		this.teamChaMap = teamChaMap;
	}

	public String getHelpRewardStr() {
		helpRewardStr = StringUtil.makeString(helpReward, '+', '|');
		return helpRewardStr;
	}

	public void setHelpRewardStr(String helpRewardStr) {
		this.helpRewardStr = helpRewardStr;
		this.helpReward = StringUtil.toMap(helpRewardStr, Integer.class, Long.class, '+', '|');
	}

	public Map<Integer, Long> getHelpReward() {
		return helpReward;
	}

	public void setHelpReward(Map<Integer, Long> helpReward) {
		this.helpReward = helpReward;
	}

	public String getHaveHelpPlayer() {
		StringBuffer sb = new StringBuffer();
		for(Long playerId : haveHelpPlayerSet){
			if(sb.length()==0){
				sb.append(playerId);
			}else{
				sb.append(",").append(playerId);
			}
		}
		haveHelpPlayer = sb.toString();
		return haveHelpPlayer;
	}

	public void setHaveHelpPlayer(String haveHelpPlayer) {
		this.haveHelpPlayer = haveHelpPlayer;
		Set<Long> tempSet = new HashSet<>();
		if(StringUtil.isNotEmpty(haveHelpPlayer)){			
			String[] arr = haveHelpPlayer.split(",");
			for(String id : arr){
				tempSet.add(Long.valueOf(id));
			}
		}
		this.haveHelpPlayerSet = tempSet;
	}

	public Set<Long> getHaveHelpPlayerSet() {
		return haveHelpPlayerSet;
	}

	public void setHaveHelpPlayerSet(Set<Long> haveHelpPlayerSet) {
		this.haveHelpPlayerSet = haveHelpPlayerSet;
	}

	public long getHelpRewardUpdateTime() {
		return helpRewardUpdateTime;
	}

	public void setHelpRewardUpdateTime(long helpRewardUpdateTime) {
		this.helpRewardUpdateTime = helpRewardUpdateTime;
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "rolerunedungeon", " roleid='" + this.getRoleId() + "'");
	}

	@Override
	public String getDeleteSql() {
//		return SqlUtil.getDeleteSql("role", " rolerunedungeon='" + this.getRoleId() + "'");
		return null;
	}

}
