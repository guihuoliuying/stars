package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.CampTeamMember;
import com.stars.modules.camp.pojo.CampCityFightData;
import com.stars.modules.camp.prodata.*;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClientCampCiytFight extends PlayerPacket {
	
	public static final byte SEND_MAIN_UI_INFO = 1;//主界面信息  齐楚之战
	public static final byte SEND_CITY_INFO = 2;//选择城池界面信息
	public static final byte SEND_READY_FIGHT = 3;//准备战斗 界面信息
	public static final byte FIGHT_END = 4;//战斗结算
	public static final byte CANCEL_MATCHING_SUCCESS = 5;//取消匹配成功
	public static final byte UPDATE_INTEGRAL = 6;//更新积分
	public static final byte SEND_MATCHTINE = 7;//匹配时间
	
	private byte opType;
	
	private int campType;
	
	private int cityId;
	
	private int cityFightNum;//已挑战次数
	
	private int commonOfficerId;
	
	private int designateOfficerId;
    
	private int rareOfficerId;
	
	private List<CampPlayerImageData> enemyList;
	
	private Map<Long, BaseTeamMember> members;
	
	private Map<Integer, Integer> awardMap;
	
	private byte result;//战斗结果
	
	private int scale;//收益比例
	
	private byte continueFight;
	
	private List<CampCityFightData> integralList;
	
	private int matchTime = 0;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return CampPackset.Client_CampCiytFight;
	}
	
	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeByte(opType);
		if(opType==SEND_MAIN_UI_INFO){
			writeMainUiInfo(buff);
		}else if(opType==SEND_CITY_INFO){
			writeCityInfo(buff);
		}else if(opType==SEND_READY_FIGHT){
			writeReadyFightInfo(buff);
		}else if(opType==FIGHT_END){
			writeFightEnd(buff);
		}else if(opType==UPDATE_INTEGRAL){
			int size = integralList.size();
			buff.writeByte((byte)size);
			CampCityFightData cityFightData = null;
			for(int i=0;i<size;i++){
				cityFightData = integralList.get(i);
				buff.writeString(String.valueOf(cityFightData.getRoleId()));
				buff.writeInt(cityFightData.getIntegral());
			}
		}else if(opType==SEND_MATCHTINE){
			buff.writeInt(matchTime);
		}
	}
	
	private void writeMainUiInfo(com.stars.network.server.buffer.NewByteBuffer buff){//主界面信息
		CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
		Integer cityLevel = campCityVo.getLevel();
		int enemyCampType = CampManager.getEnemyCampType(campType);
		CampCityVo enemyCampCityVo = CampManager.campCityLvListMap.get(enemyCampType).get(cityLevel);
		//检测城市是否满足推荐条件
		if(!checkCity(enemyCampCityVo, enemyCampType, cityLevel)){
			enemyCampCityVo = null;
			AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(enemyCampType);
			List<CampCityVo> campCityList = allServerCampPo.getOpenedCampCityList();
			int size = campCityList.size();
			CampCityVo cityVo = null;
			int minAbs = -1;
			int tempAbs = 0;
			for(int i=0;i<size;i++){
				cityVo = campCityList.get(i);
				if(!checkCity(cityVo, enemyCampType, cityLevel)) continue;
				tempAbs = Math.abs(cityVo.getLevel()-cityLevel);
				if(minAbs==-1){
					minAbs = tempAbs;
					enemyCampCityVo = cityVo;
				}else if(tempAbs<minAbs){
					minAbs = tempAbs;
					enemyCampCityVo = cityVo;
				}
			}
		}
		int enemyCityId = 0;
		String enemyCityName = "无";
		if(enemyCampCityVo!=null){
			enemyCityId = enemyCampCityVo.getId();
			enemyCityName = enemyCampCityVo.getName();
		}
		
		buff.writeInt(commonOfficerId);//普通
		buff.writeInt(designateOfficerId);//任命
		buff.writeInt(rareOfficerId);//稀有
		buff.writeInt(enemyCityId);//城池id
		buff.writeString(enemyCityName);//推荐城池名
		buff.writeInt(cityFightNum);//已挑战次数
	}
	
	private boolean checkCity(CampCityVo campCityVo, int enemyCampType, int cityLevel){//检测城市是否满足推荐条件
//		 = CampManager.campCityLvListMap.get(enemyCampType).get(cityLevel);
		Integer num = ServiceHelper.campLocalMainService().getCityPlayerNumMap().get(campCityVo.getId());
		if(num==null){
			num = 0;
		}
		if(campCityVo.getLevel()>cityLevel&&num<CampManager.Cha_CityPlayNum_Limit) return false;//敌方城池人数不足
		AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(enemyCampType);
		List<CampCityVo> campCityList = allServerCampPo.getOpenedCampCityList();
//		List<CampCityVo> campAllCityVoList = allServerCampPo.getCampCityVoList();
		if(!campCityList.contains(campCityVo)) return false;//未开发城池
		int levelSub = campCityVo.getLevel()-cityLevel;
		if(levelSub>0&&levelSub>CampManager.cityLvLimit[1]){//超过
			return false;
		}
		if(levelSub<0&&Math.abs(levelSub)>CampManager.cityLvLimit[0]){//低于
			return false;
		}
		return true;
	}
	
	private void writeCityInfo(com.stars.network.server.buffer.NewByteBuffer buff){//选择城池界面信息
		buff.writeInt(campType);//自己的阵营类型
		buff.writeInt(cityId);//自身所属城池id
		int enemyCampType = CampManager.getEnemyCampType(campType);
		writeByteCampType(campType, buff);
		writeByteCampType(enemyCampType, buff);
	}
	
	private void writeByteCampType(int wCampType, com.stars.network.server.buffer.NewByteBuffer buff){
		AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(wCampType);
		CampAtrVo campAtrVo = CampManager.campAtrMap.get(wCampType);
		buff.writeString(campAtrVo.getName());//阵营名
		buff.writeInt(wCampType);//阵营类型
		buff.writeInt(allServerCampPo.getRoleNum());//人数
		buff.writeInt(allServerCampPo.getLevel());//等级
		//城池信息
		List<CampCityVo> campCityVoList = allServerCampPo.getCampCityVoList();
		List<Integer> openCityIdList = allServerCampPo.getOpenCityIdList();
		Map<Integer, Integer> cityPlayerNumMap = ServiceHelper.campLocalMainService().getCityPlayerNumMap();
		buff.writeByte((byte)campCityVoList.size());
		Integer cityRoleNum = 0;
		for(CampCityVo vo : campCityVoList){
			vo.writeBuff(buff);//城池信息
			if(openCityIdList.contains(vo.getId())){
				buff.writeByte((byte)1);//已开启
			}else{
				buff.writeByte((byte)0);//未开启
			}
			cityRoleNum = cityPlayerNumMap.get(vo.getId());
			if(cityRoleNum==null){
				cityRoleNum = 0;
			}
			buff.writeInt(cityRoleNum);//城池玩家人数
		}
	}
	
	//准备战斗界面信息
	public void writeReadyFightInfo(com.stars.network.server.buffer.NewByteBuffer buff){
//		buff.writeByte(continueFight);// 0:非继续战斗        1：继续战斗
		String campName = CampManager.campAtrMap.get(campType).getName();
		buff.writeString(campName);
		int enemyCampType = CampManager.getEnemyCampType(campType);
		String enemyCampName = CampManager.campAtrMap.get(enemyCampType).getName();
		buff.writeString(enemyCampName);
		CampCityVo enemyCampCityVo = CampManager.campCityMap.get(cityId);
		buff.writeString(enemyCampCityVo.getName());//敌方城池名
		//己方数据
		int size = members.size();
		buff.writeByte((byte)size);
		Iterator<BaseTeamMember> iterator = members.values().iterator();
		int serverId = MultiServerHelper.getDisplayServerId();
		CampTeamMember teamMember = null;
		Object[] officerInfo = null;
		for(;iterator.hasNext();){
			teamMember = (CampTeamMember)iterator.next();
			RoleCampPo roleCampPo = teamMember.getRoleCampPo();
			buff.writeString(teamMember.getName());
			buff.writeInt(teamMember.getJob());
			buff.writeInt(teamMember.getFightSocre());
			buff.writeInt(serverId);
//			officerInfo = getOfficerInfo(roleCampPo);
//			buff.writeString((String)officerInfo[0]);//官职名称
//			buff.writeInt((Integer)officerInfo[1]);//品阶
			buff.writeInt(roleCampPo.getCommonOfficerId());
			buff.writeInt(roleCampPo.getDesignateOfficerId());
			buff.writeInt(roleCampPo.getRareOfficerId());
		}
		//敌方
		int enemySize = enemyList.size();
		buff.writeByte((byte)enemySize);
		CampPlayerImageData playerImageData = null;
		FighterEntity entity = null;
		for(int i=0;i<enemySize;i++){
			playerImageData = enemyList.get(i);
			entity = playerImageData.getEntity();
			buff.writeString(entity.getName());
			buff.writeInt(playerImageData.getJob());
			buff.writeInt(entity.getFightScore());
			buff.writeInt(playerImageData.getServerId());
//			officerInfo = getOfficerInfo(playerImageData.getCommonOfficerId(), playerImageData.getDesignateOfficerId(), 
//					playerImageData.getRareOfficerId());
//			buff.writeString((String)officerInfo[0]);//官职名称
//			buff.writeInt((Integer)officerInfo[1]);//品阶
			buff.writeInt(playerImageData.getCommonOfficerId());
			buff.writeInt(playerImageData.getDesignateOfficerId());
			buff.writeInt(playerImageData.getRareOfficerId());
		}
	}
	
	private Object[] getOfficerInfo(RoleCampPo roleCamp){
		String officerName = "";
    	int officerLevel = 0;
    	int officerQuality = 0;
    	CommonOfficerVo commonOfficer = roleCamp.getCommonOfficer();
    	officerName = commonOfficer.getName();
    	officerLevel = commonOfficer.getLevel();
    	DesignateOfficerVo designateOfficer = roleCamp.getDesignateOfficer();
    	if(designateOfficer!=null){
    		officerName = designateOfficer.getName();
    		officerQuality = designateOfficer.getQuality();
    	}
    	RareOfficerVo rareOfficer = roleCamp.getRareOfficer();
    	if(rareOfficer!=null){
    		officerName = rareOfficer.getName();
    		officerQuality = rareOfficer.getQuality();
    	}
    	return new Object[]{officerName, officerQuality, officerLevel};
	}
	
	private Object[] getOfficerInfo(int commonOfficerId, int designateOfficerId, int rareOfficerId){
		String officerName = "";
    	int officerLevel = 0;
    	int officerQuality = 0;
    	if(commonOfficerId!=0){    		
    		CommonOfficerVo commonOfficer = CampManager.commonOfficerMap.get(commonOfficerId);
    		officerName = commonOfficer.getName();
    		officerLevel = commonOfficer.getLevel();
    		DesignateOfficerVo designateOfficer = CampManager.designateOfficerMap.get(designateOfficerId);
    		if(designateOfficer!=null){
    			officerName = designateOfficer.getName();
    			officerQuality = designateOfficer.getQuality();
    		}
    		RareOfficerVo rareOfficer = CampManager.rareOfficerMap.get(rareOfficerId);
    		if(rareOfficer!=null){
    			officerName = rareOfficer.getName();
    			officerQuality = rareOfficer.getQuality();
    		}
    	}else{
    		CommonOfficerVo commonOfficer = CampManager.commonOfficerMap.get(1);
    		officerName = commonOfficer.getName();
    		officerLevel = commonOfficer.getLevel();
    	}
    	return new Object[]{officerName, officerQuality, officerLevel};
	}
	
	private void writeFightEnd(NewByteBuffer buff){
		buff.writeByte(result);
		buff.writeInt(scale);
		//玩家积分列表
		int size = integralList.size();
		buff.writeByte((byte)size);
		CampCityFightData cityFightData = null;
		for(int i=0;i<size;i++){
			cityFightData = integralList.get(i);
			buff.writeString(cityFightData.getEntity().getName());
			buff.writeInt(cityFightData.getJob());
			buff.writeInt(cityFightData.getIntegral());
			buff.writeInt(cityFightData.getServerId());
		}
		//奖励
		int awardSize = awardMap.size();
		buff.writeByte((byte)awardSize);
		Iterator<Entry<Integer, Integer>> iterator = awardMap.entrySet().iterator();
		Entry<Integer, Integer> entry = null;
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

	public int getCampType() {
		return campType;
	}

	public void setCampType(int campType) {
		this.campType = campType;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getCityFightNum() {
		return cityFightNum;
	}

	public void setCityFightNum(int cityFightNum) {
		this.cityFightNum = cityFightNum;
	}

	public List<CampPlayerImageData> getEnemyList() {
		return enemyList;
	}

	public void setEnemyList(List<CampPlayerImageData> enemyList) {
		this.enemyList = enemyList;
	}

	public Map<Long, BaseTeamMember> getMembers() {
		return members;
	}

	public void setMembers(Map<Long, BaseTeamMember> members) {
		this.members = members;
	}

	public Map<Integer, Integer> getAwardMap() {
		return awardMap;
	}

	public void setAwardMap(Map<Integer, Integer> awardMap) {
		this.awardMap = awardMap;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public List<CampCityFightData> getIntegralList() {
		return integralList;
	}

	public void setIntegralList(List<CampCityFightData> integralList) {
		this.integralList = integralList;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public byte getContinueFight() {
		return continueFight;
	}

	public void setContinueFight(byte continueFight) {
		this.continueFight = continueFight;
	}

	public int getCommonOfficerId() {
		return commonOfficerId;
	}

	public void setCommonOfficerId(int commonOfficerId) {
		this.commonOfficerId = commonOfficerId;
	}

	public int getDesignateOfficerId() {
		return designateOfficerId;
	}

	public void setDesignateOfficerId(int designateOfficerId) {
		this.designateOfficerId = designateOfficerId;
	}

	public int getRareOfficerId() {
		return rareOfficerId;
	}

	public void setRareOfficerId(int rareOfficerId) {
		this.rareOfficerId = rareOfficerId;
	}

	public int getMatchTime() {
		return matchTime;
	}

	public void setMatchTime(int matchTime) {
		this.matchTime = matchTime;
	}

}
