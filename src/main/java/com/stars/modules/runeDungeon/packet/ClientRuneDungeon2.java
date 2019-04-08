package com.stars.modules.runeDungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
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

public class ClientRuneDungeon2 extends PlayerPacket {
	
	private byte opType;
	
	private byte playType;//单人（0）还是组队（1）
	
	private int dungeonId;//副本id
	
	private int singleCha;//单人挑战进度
	
	private int angerLevel;//怒气等级
	
	private long myRoleId;
	
	private Set<Long> coolingSet;
	
	private List<Object[]> dungeonInfolist;
	
	private byte fightResult;
	
	private Map<Integer, Integer> passAward = new HashMap<>();//击破奖励
	
	private Map<Integer, Integer> allPassAward = new HashMap<>();//全部击破奖励

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
		if(opType==RuneDungeonManager.SEND_UI_INFO){
			writeUiInfo(buff);
		}else if(opType==RuneDungeonManager.SEND_DUNGEON_INFO){
			writeDungeonInfos(buff);
		}else if(opType==RuneDungeonManager.FIGHT_END){
			writeFightEnd(buff);
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
		Integer stageId = stageIdList.get(singleCha);
		Map<Integer, RuneDungeonStageInfo> stageInfoMap = runeDungeonVo.getStageInfoMap();
		RuneDungeonStageInfo stageInfo = stageInfoMap.get(stageId);
		RoleModule roleModule = module(MConst.Role);
		int fightScore = roleModule.getRoleRow().getFightScore();
		DropModule dropModule = module(MConst.Drop);
		int killAward = stageInfo.getKillAward();
		Map<Integer, Integer> showMap = dropModule.getShowItemByDropGroup(killAward);
		int killAllAward = runeDungeonVo.getSinglecompletedrop();
		Map<Integer, Integer> allAwardMap = dropModule.getShowItemByDropGroup(killAllAward);
		buff.writeInt(dungeonId);//副本id
		buff.writeString(runeDungeonVo.getDungeonname());//副本名
		buff.writeInt(stageInfo.getRecommend());//推荐战力
//		buff.writeInt(fightScore);//我的战力
		int size = 0;
		Iterator<Entry<Integer, Integer>> iterator = null;
		Entry<Integer, Integer> entry = null;
		//击破关卡奖励
		if(showMap!=null){			
			size = showMap.size();
		}
		buff.writeByte((byte)size);
		if(size>0){			
			iterator = showMap.entrySet().iterator();
			for(;iterator.hasNext();){
				entry = iterator.next();
				buff.writeInt(entry.getKey());//道具id
				buff.writeInt(entry.getValue());//道具数量
			}
		}
		//全部击破奖励
		size = 0;
		if(allAwardMap!=null){			
			size = allAwardMap.size();
		}
		buff.writeByte((byte)size);
		if(size>0){			
			iterator = allAwardMap.entrySet().iterator();
			for(;iterator.hasNext();){
				entry = iterator.next();
				buff.writeInt(entry.getKey());//道具id
				buff.writeInt(entry.getValue());//道具数量
			}
		}
		//boss
		buff.writeInt(singleCha+1);//当前进度
		size = stageIdList.size();
		buff.writeByte((byte)size);
		for(int i=0;i<size;i++){
			stageInfo = stageInfoMap.get(stageIdList.get(i));
			buff.writeString(stageInfo.getShowname());
			buff.writeInt(stageInfo.getShowmodel());
		}
	}
	
	/**
	 * 好友助战界面
	 */
	private void writeTeamUI(NewByteBuffer buff){
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		List<Integer> stageIdList = runeDungeonVo.getStageIdList();
		Map<Integer, RuneDungeonStageInfo> stageInfoMap = runeDungeonVo.getStageInfoMap();
		DropModule dropModule = module(MConst.Drop);
		List<Integer> multiKilldropList = runeDungeonVo.getMultiKilldropList();
		int index = angerLevel;
		if(index>=multiKilldropList.size()){
			index = multiKilldropList.size()-1;
		}
		int killAward = multiKilldropList.get(index);
		Map<Integer, Integer> showMap = dropModule.getShowItemByDropGroup(killAward);
		
		List<Integer> multicompletedropList = runeDungeonVo.getMulticompletedropList();
		index = angerLevel;
		if(index>=multicompletedropList.size()){
			index = multicompletedropList.size()-1;
		}
		int killAllAward = multicompletedropList.get(index);
		Map<Integer, Integer> allAwardMap = dropModule.getShowItemByDropGroup(killAllAward);
		buff.writeInt(dungeonId);//副本id
		buff.writeString(runeDungeonVo.getDungeonname());//副本名
		buff.writeInt(angerLevel);//怒气等级
		buff.writeInt(runeDungeonVo.getReqpower());//消耗体力
		int size = 0;
		Iterator<Entry<Integer, Integer>> iterator = null;
		Entry<Integer, Integer> entry = null;
		//击破关卡奖励
		if(showMap!=null){			
			size = showMap.size();
		}
		buff.writeByte((byte)size);
		if(size>0){			
			iterator = showMap.entrySet().iterator();
			for(;iterator.hasNext();){
				entry = iterator.next();
				buff.writeInt(entry.getKey());//道具id
				buff.writeInt(entry.getValue());//道具数量
			}
		}
		//全部击破奖励
		size = 0;
		if(allAwardMap!=null){			
			size = allAwardMap.size();
		}
		buff.writeByte((byte)size);
		if(size>0){			
			iterator = allAwardMap.entrySet().iterator();
			for(;iterator.hasNext();){
				entry = iterator.next();
				buff.writeInt(entry.getKey());//道具id
				buff.writeInt(entry.getValue());//道具数量
			}
		}
		//boss
		buff.writeInt(singleCha+1);//当前进度
		size = stageIdList.size();
		buff.writeByte((byte)size);
		RuneDungeonStageInfo stageInfo = null;
		for(int i=0;i<size;i++){
			stageInfo = stageInfoMap.get(stageIdList.get(i));
			buff.writeString(stageInfo.getShowname());
			buff.writeInt(stageInfo.getShowmodel());
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
			if(coolingSet.contains(friendId)){
				buff.writeByte(RuneDungeonManager.STATE_COOLING);
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

	public Set<Long> getCoolingSet() {
		return coolingSet;
	}

	public void setCoolingSet(Set<Long> coolingSet) {
		this.coolingSet = coolingSet;
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

}
