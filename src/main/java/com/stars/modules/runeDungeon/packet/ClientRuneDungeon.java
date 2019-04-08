package com.stars.modules.runeDungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.runeDungeon.RuneDungeonManager;
import com.stars.modules.runeDungeon.RuneDungeonPacketSet;
import com.stars.modules.runeDungeon.proData.RuneDungeonStageInfo;
import com.stars.modules.runeDungeon.proData.RuneDungeonVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.*;
import java.util.Map.Entry;

public class ClientRuneDungeon extends PlayerPacket {
	
	private byte opType;
	
	private byte playType;//单人（0）还是组队（1）
	
	private int dungeonId;//副本id
	
	private int singleCha;//单人挑战进度
	
	private int chaStep;//助战进度
	
	private int angerLevel;//怒气等级
	
	private long myRoleId;
	
	private Map<Long, Integer> coolingMap;
	
	private List<Object[]> dungeonInfolist;
	
	private byte fightResult;
	
	private Map<Integer, Integer> passAward = new HashMap<>();//击破奖励
	
	private Map<Integer, Integer> allPassAward = new HashMap<>();//全部击破奖励
	
	private Map<Integer, Long> helpReward = new HashMap<>();//助战奖励
	
	private Set<Long> friendSet = new HashSet<>();
	
	private List<Integer> teamStageIdList;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return RuneDungeonPacketSet.Client_RuneDungeon;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(opType);
		if(opType==RuneDungeonManager.SEND_UI_INFO){
			writeUiInfo(buff);
		}else if(opType==RuneDungeonManager.SEND_DUNGEON_INFO){
			writeDungeonInfos(buff);
		}else if(opType==RuneDungeonManager.FIGHT_END){
			writeFightEnd(buff);
		}else if(opType==RuneDungeonManager.HELP_AWARD_UI){
			writeHelpAwardUI(buff);
		}else if(opType==RuneDungeonManager.NOTICE_NEXT){
			writeNoticeNext(buff);
		}
	}
	
	private void writeUiInfo(NewByteBuffer buff){
		buff.writeByte(playType);
		if(playType==0){
			writeSigleUI(buff);
		}else{
			writeTeamUI(buff);
		}
	}
	
	/**
	 * 单人界面
	 */
	private void writeSigleUI(NewByteBuffer buff){
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		List<Integer> stageIdList = runeDungeonVo.getStageIdList();
		int indexSingle = singleCha;
		if(singleCha>=stageIdList.size()){
			indexSingle = stageIdList.size()-1;
		}
		Integer stageId = stageIdList.get(indexSingle);
		Map<Integer, RuneDungeonStageInfo> stageInfoMap = runeDungeonVo.getStageInfoMap();
		RuneDungeonStageInfo stageInfo = stageInfoMap.get(stageId);
		int killAward = stageInfo.getKillAward();
		int killAllAward = runeDungeonVo.getSinglecompletedrop();
		buff.writeInt(dungeonId);//副本id
		buff.writeString(runeDungeonVo.getDungeonname());//副本名
		buff.writeInt(stageInfo.getRecommend());//推荐战力
		buff.writeInt(runeDungeonVo.getReqpower());//消耗体力
		//击破关卡奖励
		buff.writeInt(killAward);
		//全部击破奖励
		buff.writeInt(killAllAward);
		//boss
		buff.writeInt(singleCha+1);//当前进度
		int size = stageIdList.size();
		buff.writeByte((byte)size);
		for(int i=0;i<size;i++){
			stageInfo = stageInfoMap.get(stageIdList.get(i));
			buff.writeString(stageInfo.getShowname());
			buff.writeInt(stageInfo.getShowmodel());
			buff.writeString(stageInfo.getShowicon());
		}
	}
	
	/**
	 * 好友助战界面
	 */
	private void writeTeamUI(NewByteBuffer buff){
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		List<Integer> stageIdList = teamStageIdList;
		Map<Integer, RuneDungeonStageInfo> stageInfoMap = runeDungeonVo.getStageInfoMap();
		List<Integer> multiKilldropList = runeDungeonVo.getMultiKilldropList();
		int index = angerLevel;
		if(index>=multiKilldropList.size()){
			index = multiKilldropList.size()-1;
		}
		int killAward = multiKilldropList.get(index);
		
		List<Integer> multicompletedropList = runeDungeonVo.getMulticompletedropList();
		index = angerLevel;
		if(index>=multicompletedropList.size()){
			index = multicompletedropList.size()-1;
		}
		int killAllAward = multicompletedropList.get(index);
		buff.writeInt(dungeonId);//副本id
		buff.writeString(runeDungeonVo.getDungeonname());//副本名
		buff.writeInt(angerLevel);//怒气等级
		buff.writeInt(runeDungeonVo.getReqpower());//消耗体力
		int size = 0;
		//击破关卡奖励
		buff.writeInt(killAward);
		//全部击破奖励
		buff.writeInt(killAllAward);
		//boss
		buff.writeInt(chaStep+1);//当前进度
		size = stageIdList.size();
		buff.writeByte((byte)size);
		RuneDungeonStageInfo stageInfo = null;
		for(int i=0;i<size;i++){
			stageInfo = stageInfoMap.get(stageIdList.get(i));
			buff.writeString(stageInfo.getShowname());
			buff.writeInt(stageInfo.getShowmodel());
			buff.writeString(stageInfo.getShowicon());
		}
		//好友信息
		List<Long> friendList = ServiceHelper.friendService().getFriendList(myRoleId);
		List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(friendList);
		size = summaryList.size();
		buff.writeInt((byte)size);
		Summary summary = null;
		RoleSummaryComponent comp = null;
		long friendId = 0;
		for(int i=0;i<size;i++){
			summary = summaryList.get(i);
			friendId = summary.getRoleId();
			comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
			buff.writeString(String.valueOf(friendId));//角色id
			buff.writeString(comp.getRoleName());//名称
			buff.writeInt(comp.getRoleJob());//职业
			buff.writeInt(comp.getRoleLevel());//等级
			buff.writeInt(comp.getFightScore());//战力
			if(coolingMap.containsKey(friendId)){
				buff.writeByte(RuneDungeonManager.STATE_COOLING);
				buff.writeInt(coolingMap.get(friendId));
			}else{
				buff.writeByte(RuneDungeonManager.STATE_FREE);
			}
		}
	}
	
	/**
	 * 副本信息
	 */
	public void writeDungeonInfos(NewByteBuffer buff){
		int size = dungeonInfolist.size();
		Object[] info = null;
		buff.writeInt(size);
		for(int i=0;i<size;i++){
			info = dungeonInfolist.get(i);
			buff.writeInt((Integer)info[0]);//副本id
			buff.writeString((String)info[1]);//副本名称
			buff.writeInt((Integer)info[2]);//推荐等级
			buff.writeInt((Integer)info[3]);//推荐战力
		}
	}
	
	/**
	 * 结算
	 */
	public void writeFightEnd(NewByteBuffer buff){
		buff.writeByte(fightResult);//0失败  1胜利
		int size = passAward.size();
		buff.writeByte((byte)size);
		Iterator<Entry<Integer, Integer>> iterator = passAward.entrySet().iterator();
		Entry<Integer, Integer> entry = null;
		for(;iterator.hasNext();){
			entry = iterator.next();
			buff.writeInt(entry.getKey());
			buff.writeInt(entry.getValue());
		}
		int allSize = allPassAward.size();
		buff.writeByte((byte)allSize);
		iterator = allPassAward.entrySet().iterator();
		for(;iterator.hasNext();){
			entry = iterator.next();
			buff.writeInt(entry.getKey());
			buff.writeInt(entry.getValue());
		}
	}
	
	//助战奖励界面
	public void writeHelpAwardUI(NewByteBuffer buff){
		List<Long> friendList = new ArrayList<>(friendSet);
		List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(friendList);
		int size = summaryList.size();
		buff.writeInt(size);
		Summary summary = null;
		RoleSummaryComponent comp = null;
		for(int i=0;i<size;i++){
			summary = summaryList.get(i);
			comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
			buff.writeString(comp.getRoleName());//名称
			buff.writeInt(comp.getRoleJob());//职业
			buff.writeInt(comp.getRoleLevel());//等级
		}
		
		size = helpReward.size();
		buff.writeInt(size);
		Iterator<Entry<Integer, Long>> iterator = helpReward.entrySet().iterator();
		Entry<Integer, Long> entry = null;
		for(;iterator.hasNext();){
			entry = iterator.next();
			buff.writeInt(entry.getKey());//道具id
			buff.writeString(String.valueOf(entry.getValue()));//数量   long
		}
	}
	
	public void writeNoticeNext(NewByteBuffer buff){
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		buff.writeInt(dungeonId);
		buff.writeString(runeDungeonVo.getDungeonname());
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
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

	public int getChaStep() {
		return chaStep;
	}

	public void setChaStep(int chaStep) {
		this.chaStep = chaStep;
	}

	public byte getPlayType() {
		return playType;
	}

	public void setPlayType(byte playType) {
		this.playType = playType;
	}

	public int getAngerLevel() {
		return angerLevel;
	}

	public void setAngerLevel(int angerLevel) {
		this.angerLevel = angerLevel;
	}

	public long getMyRoleId() {
		return myRoleId;
	}

	public void setMyRoleId(long myRoleId) {
		this.myRoleId = myRoleId;
	}

	public Map<Long, Integer> getCoolingMap() {
		return coolingMap;
	}

	public void setCoolingMap(Map<Long, Integer> coolingMap) {
		this.coolingMap = coolingMap;
	}

	public List<Object[]> getDungeonInfolist() {
		return dungeonInfolist;
	}

	public void setDungeonInfolist(List<Object[]> dungeonInfolist) {
		this.dungeonInfolist = dungeonInfolist;
	}

	public byte getFightResult() {
		return fightResult;
	}

	public void setFightResult(byte fightResult) {
		this.fightResult = fightResult;
	}

	public Map<Integer, Integer> getPassAward() {
		return passAward;
	}

	public void setPassAward(Map<Integer, Integer> passAward) {
		this.passAward = passAward;
	}

	public Map<Integer, Integer> getAllPassAward() {
		return allPassAward;
	}

	public void setAllPassAward(Map<Integer, Integer> allPassAward) {
		this.allPassAward = allPassAward;
	}

	public Map<Integer, Long> getHelpReward() {
		return helpReward;
	}

	public void setHelpReward(Map<Integer, Long> helpReward) {
		this.helpReward = helpReward;
	}

	public Set<Long> getFriendSet() {
		return friendSet;
	}

	public void setFriendSet(Set<Long> friendSet) {
		this.friendSet = friendSet;
	}

	public List<Integer> getTeamStageIdList() {
		return teamStageIdList;
	}

	public void setTeamStageIdList(List<Integer> teamStageIdList) {
		this.teamStageIdList = teamStageIdList;
	}

}
