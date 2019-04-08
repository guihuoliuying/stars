package com.stars.modules.familyTask.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.familyTask.FamilyTaskManager;
import com.stars.modules.familyTask.FamilyTaskPacketSet;
import com.stars.modules.familyTask.prodata.FamilyMissionGroup;
import com.stars.modules.familyTask.prodata.FamilyMissionInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.task.userdata.FamilySeekHelp;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class ClientFamilyTask extends PlayerPacket {

    public static final byte RESP_VIEW_SELF_UI = 0x01; // 个人家族任务信息
    
    public static final byte RESP_VIEW_HELP_UI = 0x02; // 打开家族任务求助信息界面
    
    public static final byte RESP_COMMIT_SUCCESS  = 0x03; // 提交成功（个人界面）
    
    public static final byte RESP_SEEK_HELP_SUCCESS = 0x04; // 求助成功
    
    public static final byte RESP_CANCEL_SEEK_HELP = 0x05; // 取消求助
    
    public static final byte RESP_HELP_COMMIT_SUCCESS = 0x06; // 帮助提交成功
    
    public static final byte RESP_GET_AWARD_SUCCESS = 0x07; // 领取奖励成功
    
    public static final byte RESP_CHAT_TASK_INFO = 0x08; // 点击聊天信息请求任务信息
    
    public static final byte RESP_CHAT_TASK_OPEN = 0X09; // 是否可打卡家族任务
    
    public static final byte RESP_REFRESH_SELE_UI = 0X0a; // 是否可打卡家族任务
    
    public ClientFamilyTask(byte subType) {
		this.subtype = subType;
	}
    
    public ClientFamilyTask() {
		// TODO Auto-generated constructor stub
	}

    public byte subtype;
    
    private Map<Integer, Byte> familyTaskMap;
    
    private List<FamilySeekHelp> helpList;
    
    private int missionId;
    
    private int taskId;
    
    private byte awardState;
    
    private byte leftTimes;
    
    private long userRoleId;
    
    private String userName;
    
    private byte openState;

    @Override
    public short getType() {
    	return FamilyTaskPacketSet.C_FAMILYTASK;
    }
    
    @Override
    public void execPacket(Player player) {
    	
    }
    
    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_VIEW_SELF_UI:
            	writeSelfUI(buff);
            	break;
            case RESP_VIEW_HELP_UI:
            	writeHelpList(buff);
            	break;
            case RESP_COMMIT_SUCCESS:
            	buff.writeInt(taskId);
            	break;
            case RESP_SEEK_HELP_SUCCESS:
            	buff.writeInt(taskId);
            	break;
            case RESP_CANCEL_SEEK_HELP:
            	buff.writeInt(taskId);
            	break;
            case RESP_HELP_COMMIT_SUCCESS:
            	
            	break;
            case RESP_GET_AWARD_SUCCESS:
            	
            	break;
            case RESP_CHAT_TASK_INFO:
            	writeTaskInfo(buff);
            	break;
            case RESP_CHAT_TASK_OPEN:
            	buff.writeByte(openState);
            	break;
        }
    }
    
    private void writeSelfUI(com.stars.network.server.buffer.NewByteBuffer buff){
    	//大任务信息
    	writeMissionInfo(buff);
    	
    	byte finishNum = 0;
    	int size = familyTaskMap.size();
    	buff.writeByte((byte)size);
    	if(size>0){
    		Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
    		Entry<Integer, Byte> entry = null;
    		FamilyMissionGroup missionGroup = null;
    		int taskid = 0;
    		byte state = 0;
    		Map<Integer, Integer> dropMap = null;
    		DropModule drop = module(MConst.Drop);
    		for(;iterator.hasNext();){
    			entry = iterator.next();
    			taskid = entry.getKey();
    			buff.writeInt(taskid);//小任务id
    			missionGroup = FamilyTaskManager.MissionGroupMap.get(taskid);
    			buff.writeInt(missionGroup.getReqCode());//道具id
    			buff.writeInt(missionGroup.getReqCount());//需要道具数量
    			buff.writeByte(missionGroup.getHelp());//是否可以求助
				buff.writeInt(missionGroup.getBabySweepId());
    			buff.writeInt(missionGroup.getRank());
    			//奖励
    			dropMap = drop.getShowItemByDropGroup(missionGroup.getAward());//drop.executeDrop(missionGroup.getAward(), 1, true);
    			int awardSize = dropMap.size();
    			buff.writeByte((byte)awardSize);
    			for(int awardItem : dropMap.keySet()){
    				buff.writeInt(awardItem);//道具id
    				buff.writeInt(dropMap.get(awardItem));//数量
    			}
    			state = entry.getValue();
    			buff.writeByte(state);//0：未提交（未求助）；1：求助中；2：已提交
    			if(state == 2){
    				finishNum += 1;
    			}
    		}
    		buff.writeByte(finishNum);//当前已提交的任务数
    		buff.writeByte(awardState);//大任务领取状态      0 未领取   1 已领取
    		buff.writeByte(leftTimes);//剩余求助次数
    	}
    }
    
    private void writeMissionInfo(com.stars.network.server.buffer.NewByteBuffer buff){
    	if(missionId>0){
    		buff.writeByte((byte)1);
    		FamilyMissionInfo missionInfo = FamilyTaskManager.MissionInfoMap.get(missionId);
        	buff.writeString(missionInfo.getName());
        	buff.writeString(missionInfo.getImage());
        	buff.writeString(missionInfo.getDescribe());
//        	buff.writeString(missionInfo.getHelpDesc());
        	int award = missionInfo.getAward();
        	//奖励
        	DropModule drop = module(MConst.Drop);
        	Map<Integer, Integer> dropMap = drop.getShowItemByDropGroup(award);//drop.executeDrop(award, 1, true);
        	int size = dropMap.size();
        	buff.writeByte((byte)size);
        	for(int awardItem : dropMap.keySet()){
        		buff.writeInt(awardItem);//道具id
        		buff.writeInt(dropMap.get(awardItem));//数量
        	}
    	}else{
    		buff.writeByte((byte)0);
    	}
    }
    
    private void writeHelpList(com.stars.network.server.buffer.NewByteBuffer buff){
    	List<FamilySeekHelp> sendList = new ArrayList<>();
    	int size = helpList.size();
    	FamilySeekHelp familySeekHelp = null;
    	for(int i=0;i<size;i++){
    		familySeekHelp = helpList.get(i);
    		if(familySeekHelp.getRoleId()==userRoleId){
    			continue;
    		}
    		sendList.add(familySeekHelp);
    	}
    	int sendSize = sendList.size();
    	buff.writeInt(sendSize);
    	FamilyMissionGroup missionGroup = null;
    	int taskid = 0;
    	for(int i=0;i<sendSize;i++){
			familySeekHelp = sendList.get(i);
			buff.writeString(String.valueOf(familySeekHelp.getRoleId()));
			buff.writeString(familySeekHelp.getRoleName());
			taskid = familySeekHelp.getTaskId();
			buff.writeInt(taskid);
			missionGroup = FamilyTaskManager.MissionGroupMap.get(taskid);
			buff.writeInt(missionGroup.getReqCode());//道具id
			buff.writeInt(missionGroup.getReqCount());//需求道具数量
			buff.writeInt(missionGroup.getReqGold());//需求元宝
			buff.writeInt(missionGroup.getRank());
			DropModule drop = module(MConst.Drop);
			//额外奖励
			int goldAward = missionGroup.getGoldAward();
			Map<Integer, Integer> dropMap = drop.getShowItemByDropGroup(goldAward);//drop.executeDrop(goldAward, 1, true);
			int awardSize = 0;
			if(StringUtil.isNotEmpty(dropMap)){
				awardSize = dropMap.size();
			}
        	buff.writeByte((byte)awardSize);
        	if(awardSize>0){        		
        		for(int awardItem : dropMap.keySet()){
        			buff.writeInt(awardItem);//道具id
        			buff.writeInt(dropMap.get(awardItem));//数量
        		}
        	}
			//提交奖励
			int award = missionGroup.getHelpaward();
			dropMap = drop.getShowItemByDropGroup(award);//drop.executeDrop(award, 1, true);
			awardSize = 0;
			if(StringUtil.isNotEmpty(dropMap)){
				awardSize = dropMap.size();
			}
        	buff.writeByte((byte)awardSize);
        	if(awardSize>0){        		
        		for(int awardItem : dropMap.keySet()){
        			buff.writeInt(awardItem);//道具id
        			buff.writeInt(dropMap.get(awardItem));//数量
        		}
        	}
    	}
    }
    
    public void writeTaskInfo(NewByteBuffer buff){
    	buff.writeString(String.valueOf(userRoleId));
		buff.writeString(userName);
    	buff.writeInt(taskId);
    	FamilyMissionGroup missionGroup = FamilyTaskManager.MissionGroupMap.get(taskId);
		buff.writeInt(missionGroup.getReqCode());//道具id
		buff.writeInt(missionGroup.getReqCount());//需求道具数量
		buff.writeInt(missionGroup.getReqGold());//需求元宝
		DropModule drop = module(MConst.Drop);
		//额外奖励
		int goldAward = missionGroup.getGoldAward();
		Map<Integer, Integer> dropMap = drop.getShowItemByDropGroup(goldAward);//drop.executeDrop(goldAward, 1, true);
		int awardSize = 0;
		if(StringUtil.isNotEmpty(dropMap)){
			awardSize = dropMap.size();
		}
    	buff.writeByte((byte)awardSize);
    	if(awardSize>0){    		
    		for(int awardItem : dropMap.keySet()){
    			buff.writeInt(awardItem);//道具id
    			buff.writeInt(dropMap.get(awardItem));//数量
    		}
    	}
		//提交奖励
		int award = missionGroup.getHelpaward();
		dropMap = drop.getShowItemByDropGroup(award);//drop.executeDrop(award, 1, true);
		awardSize = 0;
		if(StringUtil.isNotEmpty(dropMap)){
			awardSize = dropMap.size();
		}
    	buff.writeByte((byte)awardSize);
    	if(awardSize>0){    		
    		for(int awardItem : dropMap.keySet()){
    			buff.writeInt(awardItem);//道具id
    			buff.writeInt(dropMap.get(awardItem));//数量
    		}
    	}
    }

	public Map<Integer, Byte> getFamilyTaskMap() {
		return familyTaskMap;
	}

	public void setFamilyTaskMap(Map<Integer, Byte> familyTaskMap) {
		this.familyTaskMap = familyTaskMap;
	}

	public List<FamilySeekHelp> getHelpList() {
		return helpList;
	}

	public void setHelpList(List<FamilySeekHelp> helpList) {
		this.helpList = helpList;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public byte getAwardState() {
		return awardState;
	}

	public void setAwardState(byte awardState) {
		this.awardState = awardState;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public byte getLeftTimes() {
		return leftTimes;
	}

	public void setLeftTimes(byte leftTimes) {
		this.leftTimes = leftTimes;
	}

	public long getUserRoleId() {
		return userRoleId;
	}

	public void setUserRoleId(long userRoleId) {
		this.userRoleId = userRoleId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public byte getOpenState() {
		return openState;
	}

	public void setOpenState(byte openState) {
		this.openState = openState;
	}

}
