package com.stars.modules.skyrank.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.prodata.SkyRankGradVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.skyrank.SkyRankShowData;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 玩家天梯积分
 * 
 * @author xieyuejun
 *
 */
public class SkyRankDataPo extends DbRow {

	// CREATE TABLE `roleskyrank` (
	// `roleid` bigint(3) NOT NULL,
	// `name` varchar(30) DEFAULT NULL,
	// `fightscore` int(3) DEFAULT NULL COMMENT '战力',
	// `score` int(3) DEFAULT NULL COMMENT '积分',
	// `hidescore` int(3) DEFAULT NULL COMMENT '隐藏积分',
	// `upawardrecord` text COMMENT ' 已领取的段位升级奖励',
	// `scorerecordmap` text COMMENT '积分获取信息记录',
	// `maxscore` int(3) DEFAULT NULL COMMENT '历史最高积分',
	// PRIMARY KEY (`roleid`)
	// ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	public SkyRankShowData getNewShowData(){
		SkyRankShowData newShowData = new SkyRankShowData();
		newShowData.setServerId(MultiServerHelper.getServerId());
		newShowData.setRoleId(roleId);
		newShowData.setName(name);
		newShowData.setFightscore(fightScore);
		newShowData.setScore(score);
		newShowData.setServerName(MultiServerHelper.getServerName());
		return newShowData;
	}
	
	
	private long roleId;

	private String name;

	private int fightScore;

	private int score;// 当前总积分

	private int hideScore;// 当前段位的隐藏分

	private int maxScore;// 历史最高积分
	
	private int maxRank;//历史最高排名
	
	private int awardGrad;//奖励段位（重置时玩家的段位）
	
	private int dailyAward;//每日奖励
	
	private byte dailyAwardState;//每日奖励领取状态

	private Set<Integer> upAwardRecordSet = new HashSet<>();// 已经领取的段位提升奖励

	private Map<Short, SkyRankScoreRecord> scoreRecord = new HashMap<Short, SkyRankScoreRecord>();
	
	public String toString(){
		return this.roleId +"|"+fightScore+"|"+score+"|"+hideScore+"|"+maxScore;
	}
	
	public void addLadderScoreAddRecord(short type,int addScoreTime){
		SkyRankScoreRecord record = 	scoreRecord.get(type);
		if(record == null)
		this.setUpdateStatus();
		record.addScoreTime();
	}

	public void addLadderScoreRecord(SkyRankScoreRecord record) {
		if(scoreRecord.containsKey(record.getType()))return;
		scoreRecord.put(record.getType(), record);
		this.setUpdateStatus();
	}

	public void addScore(int addScore) {
		int oldScore = score;
		score += addScore;
		// 防止溢出处理
		if (score < 0) {
			score = oldScore;
		}
		if (score > maxScore) {
			maxScore = score;
		}
		setUpdateStatus();
	}

	public void reduceScore(int reduceScore) {
		if(reduceScore >0){
			addScore(reduceScore);
			return;
		}
		
		SkyRankGradVo nowGrad = SkyRankManager.getManager().getSkyRankGradVoByScore(score);
		if(nowGrad == null)return;
		setUpdateStatus();
		
		int nowGradScore = score - nowGrad.getReqscore();
		
		nowGradScore = nowGradScore <0?0:nowGradScore;
		
		int nowSubScore = reduceScore;// 当前段应该扣的实际积分
		//有积分或隐藏分，当前段扣
		if(nowGradScore >0 || hideScore >0 ){
			//当前分还有可以扣
			if(nowGradScore >0){
				nowGradScore += reduceScore;
				//当前分不够扣
				if(nowGradScore <0){
					//实际扣的当前分
					nowSubScore = reduceScore - nowGradScore;
					//扣隐藏分
					if (hideScore > 0) {
						hideScore += nowGradScore;
						if(hideScore <0){
							hideScore = 0;
						}
					} 
				}
			} else {
				// 当前没分扣了
				// 扣隐藏分
				hideScore += reduceScore;
				if (hideScore < 0) {
					hideScore = 0;
				}
				//有隐藏分扣，实际的就不扣了
				nowSubScore =0;
			}
		}
		score += nowSubScore;
		if (score < 0) {
			score = 0;
		}
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roleskyrank", " roleid=" + this.getRoleId());
	}

	@Override
	public String getDeleteSql() {
		return  "delete from `roleskyrank` where `roleid`=" + this.getRoleId();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(this.name == null || !this.name.equals(name)){
			setUpdateStatus();
		}
		this.name = name;
	}

	public int getFightScore() {
		return fightScore;
	}

	public void setFightScore(int fightScore) {
		if(this.fightScore != fightScore){
			setUpdateStatus();
		}
		this.fightScore = fightScore;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getHideScore() {
		return hideScore;
	}

	public void setHideScore(int hideScore) {
		if(this.hideScore != hideScore){
			setUpdateStatus();
		}
		this.hideScore = hideScore;
	}
	
	public boolean containsUpAwardRecord(int gradId) {
		return upAwardRecordSet.contains(gradId);
	}
	
	public void addUpAwardRecord(int gradId) {
		upAwardRecordSet.add(gradId);
		this.setUpdateStatus();
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public Set<Integer> getUpUpAwardRecordSet() {
		return upAwardRecordSet;
	}

	public void setUpUpAwardRecordSet(Set<Integer> upAwardRecordSet) {
		this.upAwardRecordSet = upAwardRecordSet;
	}

	public Map<Short, SkyRankScoreRecord> getScoreRecord() {
		return scoreRecord;
	}

	public void setScoreRecord(Map<Short, SkyRankScoreRecord> scoreRecord) {
		this.scoreRecord = scoreRecord;
	}
	
	////////////////////////////////////////////

	public int getAwardGrad() {
		return awardGrad;
	}

	public void setAwardGrad(int awardGrad) {
		this.awardGrad = awardGrad;
	}

	public int getDailyAward() {
		return dailyAward;
	}

	public void setDailyAward(int dailyAward) {
		this.dailyAward = dailyAward;
	}

	public byte getDailyAwardState() {
		return dailyAwardState;
	}

	public void setDailyAwardState(byte dailyAwardState) {
		this.dailyAwardState = dailyAwardState;
	}

	public String getUpAwardRecord() {
		if(this.upAwardRecordSet != null && this.upAwardRecordSet.size() >0){
			return StringUtil.makeString(this.upAwardRecordSet, ',');
		}else{
			return "";
		}
	}

	public void setUpAwardRecord(String upAwardRecord) {
		Set<Integer> upAwardRecordSet = new HashSet<>();// 已经领取的段位提升奖励
		if(upAwardRecord != null && upAwardRecord.trim().length() >0){
			String[] records = upAwardRecord.split(",");
			for(String rec:records){
				if(rec != null && rec.trim().length() >0){
					upAwardRecordSet.add(Integer.parseInt(rec));
				}
			}
		}
		this.upAwardRecordSet = upAwardRecordSet;
	}

	public String getScoreRecordMap() {
		if(this.scoreRecord != null && this.scoreRecord.size() >0){
			StringBuilder sbuff = new StringBuilder();
			for(SkyRankScoreRecord record:this.scoreRecord.values()){
				if(sbuff.length() >0){
					sbuff.append("&");
				}
				sbuff.append(record.toString());
			}
			return sbuff.toString();
		}
		return "";
	}

	public void setScoreRecordMap(String scoreRecordMap) {
		Map<Short, SkyRankScoreRecord> scoreRecord = new HashMap<Short, SkyRankScoreRecord>();
		if(scoreRecordMap != null && scoreRecordMap.trim().length() > 0){
			String[] records = scoreRecordMap.split("&");
			for(String record:records){
				if(record != null && record.trim().length() >0){
					SkyRankScoreRecord sr = new SkyRankScoreRecord(record);
					scoreRecord.put(sr.getType(), sr);
				}
			}
		}
		this.scoreRecord = scoreRecord;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public void setMaxRank(int maxRank) {
		if(maxRank != this.maxRank){
			this.setUpdateStatus();
		}
		this.maxRank = maxRank;
	}

	
	
}
