package com.stars.multiserver.LootTreasure;

import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.LootTreasureRangeParam;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MultiServerHelper;
import com.stars.util.I18n;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhou
 *
 *夺宝人
 *
 */
public class Looter {

	private FighterEntity fiEntity;

	private Long id;
	private int jobId;

	private int serverId;
	private String serverName;

	/**
	 * 下一次匹配时间
	 */
	private long nextMatchTime;

	private LTRoom room;

	/**
	 * 房间选择表示,设置要使用set接口;
	 */
	private byte room_flag;

	public Map<Long, Integer>hurts;

	/**
	 * 夺宝的PVE伤害记录;
	 */
	public LTDamageRankVo pveLtDamageRankVo;
	/**
	 * 夺宝的PVP击杀记录;
	 */
	public LTDamageRankVo pvpLtDamageRankVo;

	private int boxs = 0;
	/**
	 * 当前宝箱的数量上限;
	 */
	private int boxCountLimit = 0;
	/**
	 * 玩家上次成功切换房间的时间戳;
	 */
	private long lastManualSwitchRoomStamp = 0L;
	/**
	 * 标识玩家希望切换到的房间类型;
	 */
	private byte isWaitToMatchRoomType = -1;

	public Looter(int serverId, String serverName, FighterEntity fiEntity, int jobId){
		this.serverId = serverId;
		this.serverName = serverName;
		this.fiEntity = fiEntity;
		this.id = Long.parseLong(fiEntity.getUniqueId());
		this.jobId = jobId;
		setRoom_flag(PVPLootTreasure.ROOM_FLAG_LOW);
		this.hurts = new HashMap<Long, Integer>();
//		String serverNameFull = serverName + serverId;
//		String serverNameFull = serverId + "服";
		String serverNameFull = I18n.get("common.serverName", MultiServerHelper.getDisplayServerId(serverId));
//		String serverNameFull = "首测服";
		this.pveLtDamageRankVo = new LTDamageRankVo(this.id, fiEntity.getName(), fiEntity.getLevel(), jobId, serverNameFull);
		this.pvpLtDamageRankVo = new LTDamageRankVo(this.id, fiEntity.getName(), fiEntity.getLevel(), jobId, serverNameFull);
		boxs = 0;//临时处理
	}

	/**
	 * 丢失宝箱数量;
	 * @param boxCount
	 */
	public boolean loseBoxCount(int boxCount){
		if (boxCount < 0){
			return false;
		}
		if (this.boxs - boxCount < 0){
			return false;
		}
		this.boxs -= boxCount;
		return true;
	}

	/**
	 * 获得宝箱数量;
	 * @param boxCount
	 * @return 返回实际增加的宝箱;
	 */
	public int gainBoxCount(int boxCount){
		int preBoxCount = this.boxs;
		this.boxs += boxCount;
		//0代表无上限;
		if(this.boxs > boxCountLimit && boxCountLimit != 0){
			this.boxs = boxCountLimit;
			return this.boxs - preBoxCount;
		}
		return boxCount;
	}

	public void checkSwitchRoomFlag(){
		if (isWaitToMatchRoomType>=0){
			setRoom_flag(isWaitToMatchRoomType);
			lastManualSwitchRoomStamp = System.currentTimeMillis();
			isWaitToMatchRoomType = -1;
		}
	}

	//检查宝箱数量,宝箱数量是否达到上限了;
	public boolean isBoxLimit(){
		int limitBoxCount = LootTreasureManager.roomFlagBoxLimitMap.get(getRoom_flag());
		if (limitBoxCount != 0 && getBoxs() >= limitBoxCount) {
			return true;
		}
		return false;
	}

	public FighterEntity getFiEntity() {
		return fiEntity;
	}

	public void setFiEntity(FighterEntity fiEntity) {
		this.fiEntity = fiEntity;
	}

	public byte getRoom_flag() {
		return room_flag;
	}

	public void setRoom_flag(byte room_flag) {
		this.room_flag = room_flag;
		this.boxCountLimit = LootTreasureManager.roomFlagBoxLimitMap.get(this.room_flag);
	}

	public long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getNextMatchTime() {
		return nextMatchTime;
	}

	public void setNextMatchTime(long nextMatchTime) {
		this.nextMatchTime = nextMatchTime;
	}

	public void recycle(){
		this.room = null;
		this.hurts.clear();
	}

	public LTRoom getRoom() {
		return room;
	}

	public void addHurt(long initiator,int count){
		if (this.hurts.containsKey(initiator)) {
			int hasHurt = this.hurts.get(initiator);
			this.hurts.put(initiator, hasHurt + count);
			return;
		}
		this.hurts.put(initiator, count);
	}

	public  void removeHurt(long initiator){
		this.hurts.remove(initiator);
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public void setRoom(LTRoom room) {
		this.room = room;
	}

	public int getBoxs() {
		return boxs;
	}

	public void setBoxs(int boxs) {
		this.boxs = boxs;
	}

	public long getLastManualSwitchRoomStamp(){
		return this.lastManualSwitchRoomStamp;
	}

	public long getLastManualSwitchRoomEndStamp(){
		return this.lastManualSwitchRoomStamp + LootTreasureManager.PVP_SWITCH_ROOM_CD;
	}

    public void setLastManualSwitchRoomStamp(long lastManualSwitchRoomStamp){
        this.lastManualSwitchRoomStamp = lastManualSwitchRoomStamp;
    }

	/**
	 * 真正的切换房间;
	 * @param roomType
	 */
	private void reallySwitchRoom(byte roomType){
		isWaitToMatchRoomType = roomType;
	}

	/**
	 * 请求切换房间;
	 * @param roomType
	 * @return 结果反馈给客户端做处理;
	 */
	public byte requestSwitchRoom(byte roomType) {
		//判断是否满足切换房间的条件;
		LootTreasureRangeParam lootTreasureRangeParam = LootTreasureManager.pvpSwitchRoomConditionMap.get(roomType);
		byte rtnValue = roomType;
		do {
			int diffValue = lootTreasureRangeParam.isInRange(this.boxs);
			if(diffValue == 0){
				//判断CD是否到了;
				if(System.currentTimeMillis() > getLastManualSwitchRoomEndStamp()){
					isWaitToMatchRoomType = roomType;
				}else{
					rtnValue = (byte)-2; // cd不满足;
				}
			}else{
				rtnValue = (byte)-1; // 宝箱数量不满足;
			}
		}while (false);
		return  rtnValue;
	}

	public int getDiffBoxCountByRoomType(byte roomType){
		LootTreasureRangeParam lootTreasureRangeParam = LootTreasureManager.pvpSwitchRoomConditionMap.get(roomType);
		return lootTreasureRangeParam.isInRange(this.boxs);
	}

	public byte getIsWaitToMatchRoomType() {
		return isWaitToMatchRoomType;
	}

	public void setIsWaitToMatchRoomType(byte isWaitToMatchRoomType) {
		this.isWaitToMatchRoomType = isWaitToMatchRoomType;
	}

	public int getJobId() {
		return jobId;
	}

}
