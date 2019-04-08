package com.stars.modules.daily5v5.packet;

import com.stars.core.attr.Attribute;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.multiserver.daily5v5.data.Daily5v5MatchingVo;
import com.stars.multiserver.daily5v5.data.FightingEndVo;
import com.stars.multiserver.daily5v5.data.PvpExtraEffect;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClientDaily5v5 extends PlayerPacket {
	
	public ClientDaily5v5() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientDaily5v5(byte opType) {
		this.opType = opType;
	}
	
	private byte opType;
	
	private byte actState;
	
	private List<Daily5v5MatchingVo> memberList;
	
	private List<Daily5v5MatchingVo> enermyMemberList;
	
	private Attribute attribute;
	
	private Map<Integer, Integer> skillMap;
	
	private Map<Integer, Integer> skillDamageMap;
	
	private List<int[]> pvpextraeffectList;
	
	private List<int[]> extraeffectshowList;
	
	private int matchingSuccessTime;
	
	private List<FightingEndVo> winnerList;
	
	private List<FightingEndVo> loserList;
	
	private int frequency;
	
	private Map<Integer, Integer> award;
	
//	private int winMorale;
//	
//	private int loseMorale;
	
	private long winPoints;
	
	private long losePoints;
	
	private byte endType;
	
	private byte result;
	
	private int buffId;
	
	private byte matchingState;//  1 进入匹配      2退出匹配
	
	private List<int[]> buffCdList;
	
	private int reviveCd;//复活冷却时间

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.CLIENT_DAILY_5V5;
	}
	
	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeByte(opType);
		if(opType== Daily5v5Manager.OPEN_UI_INFO){
			writeOpenInfo(buff);
		}else if(opType==Daily5v5Manager.ACTIVITY_STATE){
			buff.writeByte(actState);
		}else if(opType==Daily5v5Manager.READY_FIGHT){//匹配成功准备战斗
			writeReadyFight(buff);
		}else if(opType==Daily5v5Manager.FIGHTING_END){
			writeFightEnd(buff);
		}else if(opType==Daily5v5Manager.USE_BUFF_RESULT){
			buff.writeInt(buffId);
			buff.writeByte(result);// 1 成功        0 失败（异常 外挂发包）    2 冷却中
		}else if(opType==Daily5v5Manager.RESP_JOIN_TIMES){
			buff.writeInt(frequency);//已参与次数
//			buff.writeByte((byte)0);//(Daily5v5Manager.Daily5v5TotalCount);//总次数
		}else if(opType==Daily5v5Manager.MATCHING_STATE){
			LogUtil.info("匹配服有返回啦啦啦！！！！！！！！");
			buff.writeByte(matchingState);//1 进入匹配      2退出匹配
		}else if(opType==Daily5v5Manager.BUFF_CD_INFO){
			writeBuffCDInfo(buff);
		}else if(opType==Daily5v5Manager.REVIVE_CD_INFO){
			//复活CD
			buff.writeInt(reviveCd);
		}
	}
	
	private void writeOpenInfo(com.stars.network.server.buffer.NewByteBuffer buff){
		attribute.writeToBuffer(buff);//属性
		//技能
		int size = skillMap.size();
		buff.writeByte((byte)size);
		Iterator<Entry<Integer, Integer>> iterator = skillMap.entrySet().iterator();
		Entry<Integer, Integer> entry = null;
		int maxSkillLevel = 0;
		int skillId = 0;
		int skillLevel = 0;
		SkillvupVo skillvupVo = null;
		for(;iterator.hasNext();){
			entry = iterator.next();
			skillId = entry.getKey();
			skillLevel = entry.getValue();
			buff.writeInt(skillId);//技能id
			buff.writeInt(skillLevel);//等级
			maxSkillLevel = SkillManager.getMaxSkillLevel(entry.getKey());
			buff.writeInt(maxSkillLevel);//max level
			skillvupVo = SkillManager.getSkillvupVo(skillId, skillLevel);
			SkillvupVo.writeDamageDescDataToBuff(buff, attribute.getAttack(), skillvupVo.getDamagedesc(), 
					skillvupVo.getCoefficient(), skillvupVo.getDamage());
		}
		//vip 效果
		int eSize = pvpextraeffectList.size();//已激活
		buff.writeByte((byte)eSize);
		int[] idArr = null;
		int idSize = 0;
		int effecttype = 0;
		PvpExtraEffect pvpExtraEffect = null;
		PvpExtraEffect nextPvpExtraEffect = null;
		for(int i=0;i<eSize;i++){
			idArr = pvpextraeffectList.get(i);
			idSize = idArr.length;
			buff.writeByte((byte)idSize);
			for(int j=0;j<idSize;j++){
				pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(idArr[j]);
				if(pvpExtraEffect==null){
					buff.writeByte((byte)0);
				}else{
					buff.writeByte((byte)1);
					pvpExtraEffect.write(buff);
					effecttype = pvpExtraEffect.getEffecttype();
					nextPvpExtraEffect = getNextPvpExtraEffect(effecttype, pvpExtraEffect.getLevel()+1);
					if(nextPvpExtraEffect!=null){						
						buff.writeString(nextPvpExtraEffect.getActivedesc());
						buff.writeString(nextPvpExtraEffect.getParam());
					}else{
						buff.writeString("");
						buff.writeString("");
					}
					buff.writeInt(getEffectMaxLevel(effecttype));
				}
			}
		}
		int showSize = extraeffectshowList.size();//未激活
		buff.writeByte((byte)showSize);
		for(int i=0;i<showSize;i++){
			idArr = extraeffectshowList.get(i);
			idSize = idArr.length;
			buff.writeByte((byte)idSize);
			for(int j=0;j<idSize;j++){
				pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(idArr[j]);
				if(pvpExtraEffect==null){
					buff.writeByte((byte)0);
				}else{
					buff.writeByte((byte)1);
					pvpExtraEffect.write(buff);
					effecttype = pvpExtraEffect.getEffecttype();
					nextPvpExtraEffect = getNextPvpExtraEffect(effecttype, pvpExtraEffect.getLevel()+1);
					if(nextPvpExtraEffect!=null){						
						buff.writeString(nextPvpExtraEffect.getActivedesc());
						buff.writeString(nextPvpExtraEffect.getParam());
					}else{
						buff.writeString("");
						buff.writeString("");
					}
					buff.writeInt(getEffectMaxLevel(effecttype));
				}
			}
		}
	}
	
	private PvpExtraEffect getNextPvpExtraEffect(int effectType, int nextLevel){
		List<PvpExtraEffect> list = Daily5v5Manager.effectTypeMap.get(effectType);
		int size = list.size();
		PvpExtraEffect pvpExtraEffect = null;
		for(int i=0;i<size;i++){
			pvpExtraEffect = list.get(i);
			if(pvpExtraEffect.getLevel()==nextLevel){
				return pvpExtraEffect;
			}
		}
		return null;
	}
	
	private int getEffectMaxLevel(int effectType){
		Integer maxLevel = Daily5v5Manager.effectMaxLevel.get(effectType);
		if(maxLevel==null){
			maxLevel = 0;
		}
		return maxLevel;
	}
	
	private void writeReadyFight(com.stars.network.server.buffer.NewByteBuffer buff){
		buff.writeInt(Daily5v5Manager.START_REMIND_TIME);
		buff.writeInt(matchingSuccessTime);
		int size = memberList.size();
		buff.writeByte((byte)size);
		Daily5v5MatchingVo matchingVo = null;
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);
			buff.writeString(String.valueOf(matchingVo.getRoleId()));
			buff.writeString(matchingVo.getRoleName());
			buff.writeInt(matchingVo.getFightScore());
			buff.writeInt(matchingVo.getLevel());
			buff.writeInt(matchingVo.getJob());
			buff.writeInt(matchingVo.getFixIntegral());
			buff.writeInt(matchingVo.getServerId()%1000);
		}
		int enermySize = enermyMemberList.size();
		buff.writeByte((byte)enermySize);
		for(int i=0;i<enermySize;i++){
			matchingVo = enermyMemberList.get(i);
			buff.writeString(String.valueOf(matchingVo.getRoleId()));
			buff.writeString(matchingVo.getRoleName());
			buff.writeInt(matchingVo.getFightScore());
			buff.writeInt(matchingVo.getLevel());
			buff.writeInt(matchingVo.getJob());
			buff.writeInt(matchingVo.getFixIntegral());
			buff.writeInt(matchingVo.getServerId()%1000);
		}
	}
	
	public void writeFightEnd(com.stars.network.server.buffer.NewByteBuffer buff){
		buff.writeByte(endType);//1 基地塔          2 超时
		buff.writeString(String.valueOf(winPoints));//胜利方积分
		buff.writeString(String.valueOf(losePoints));//失败方积分
		FightingEndVo fightingEndVo = null;
		int winSize = winnerList.size();
		buff.writeByte((byte)winSize);
		for(int i=0;i<winSize;i++){
			fightingEndVo = winnerList.get(i);
			buff.writeString(String.valueOf(fightingEndVo.getRoleId()));
			buff.writeString(fightingEndVo.getRoleName());
			buff.writeString(String.valueOf(fightingEndVo.getIntegral()));//积分
			buff.writeInt(fightingEndVo.getKillCount());//人头
			buff.writeInt(fightingEndVo.getAssistCount());//助攻
			buff.writeInt(fightingEndVo.getDeadCount());//阵亡
			buff.writeInt(fightingEndVo.getMaxComboKillCount());//最高连斩
		}
		int loseSize = loserList.size();
		buff.writeByte((byte)loseSize);
		for(int i=0;i<loseSize;i++){
			fightingEndVo = loserList.get(i);
			buff.writeString(String.valueOf(fightingEndVo.getRoleId()));
			buff.writeString(fightingEndVo.getRoleName());
			buff.writeString(String.valueOf(fightingEndVo.getIntegral()));
			buff.writeInt(fightingEndVo.getKillCount());//人头
			buff.writeInt(fightingEndVo.getAssistCount());//助攻
			buff.writeInt(fightingEndVo.getDeadCount());//阵亡
			buff.writeInt(fightingEndVo.getMaxComboKillCount());//最高连斩
		}
		//奖励
		Iterator<Entry<Integer, Integer>> iterator = award.entrySet().iterator();
		Entry<Integer, Integer> entry = null;
		int awSize = award.size();
		buff.writeByte((byte)awSize);
		for(;iterator.hasNext();){
			entry = iterator.next();
			buff.writeInt(entry.getKey());//道具id
			buff.writeInt(entry.getValue());//数量
		}
	}
	
	public void writeBuffCDInfo(NewByteBuffer buff){
		int size = buffCdList.size();
		buff.writeByte((byte)size);
		int[] info;
		for(int i=0;i<size;i++){
			info = buffCdList.get(i);
			buff.writeInt(info[0]);//effectId
			buff.writeInt(info[1]);//CD
		}
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getActState() {
		return actState;
	}

	public void setActState(byte actState) {
		this.actState = actState;
	}

	public List<Daily5v5MatchingVo> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<Daily5v5MatchingVo> memberList) {
		this.memberList = memberList;
	}

	public List<Daily5v5MatchingVo> getEnermyMemberList() {
		return enermyMemberList;
	}

	public void setEnermyMemberList(List<Daily5v5MatchingVo> enermyMemberList) {
		this.enermyMemberList = enermyMemberList;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public Map<Integer, Integer> getSkillMap() {
		return skillMap;
	}

	public void setSkillMap(Map<Integer, Integer> skillMap) {
		this.skillMap = skillMap;
	}

	public Map<Integer, Integer> getSkillDamageMap() {
		return skillDamageMap;
	}

	public void setSkillDamageMap(Map<Integer, Integer> skillDamageMap) {
		this.skillDamageMap = skillDamageMap;
	}

	public List<int[]> getPvpextraeffectList() {
		return pvpextraeffectList;
	}

	public void setPvpextraeffectList(List<int[]> pvpextraeffectList) {
		this.pvpextraeffectList = pvpextraeffectList;
	}

	public List<int[]> getExtraeffectshowList() {
		return extraeffectshowList;
	}

	public void setExtraeffectshowList(List<int[]> extraeffectshowList) {
		this.extraeffectshowList = extraeffectshowList;
	}

	public int getMatchingSuccessTime() {
		return matchingSuccessTime;
	}

	public void setMatchingSuccessTime(int matchingSuccessTime) {
		this.matchingSuccessTime = matchingSuccessTime;
	}

	public List<FightingEndVo> getWinnerList() {
		return winnerList;
	}

	public void setWinnerList(List<FightingEndVo> winnerList) {
		this.winnerList = winnerList;
	}

	public List<FightingEndVo> getLoserList() {
		return loserList;
	}

	public void setLoserList(List<FightingEndVo> loserList) {
		this.loserList = loserList;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public Map<Integer, Integer> getAward() {
		return award;
	}

	public void setAward(Map<Integer, Integer> award) {
		this.award = award;
	}

	public long getWinPoints() {
		return winPoints;
	}

	public void setWinPoints(long winPoints) {
		this.winPoints = winPoints;
	}

	public long getLosePoints() {
		return losePoints;
	}

	public void setLosePoints(long losePoints) {
		this.losePoints = losePoints;
	}

	public byte getEndType() {
		return endType;
	}

	public void setEndType(byte endType) {
		this.endType = endType;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public byte getMatchingState() {
		return matchingState;
	}

	public void setMatchingState(byte matchingState) {
		this.matchingState = matchingState;
	}

	public List<int[]> getBuffCdList() {
		return buffCdList;
	}

	public void setBuffCdList(List<int[]> buffCdList) {
		this.buffCdList = buffCdList;
	}

	public int getReviveCd() {
		return reviveCd;
	}

	public void setReviveCd(int reviveCd) {
		this.reviveCd = reviveCd;
	}

}
