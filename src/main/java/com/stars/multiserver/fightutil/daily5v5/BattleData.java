package com.stars.multiserver.fightutil.daily5v5;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.daily5v5.data.Daily5v5MatchingVo;
import com.stars.multiserver.daily5v5.data.MatchingTeamVo;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightArgs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleData {
	
	private long camp1Id;
	
    private long camp2Id;
    
    private String fightId;
    
    private String camp1Name;
    
    private String camp2Name;
    
    private Map<String, FighterEntity> camp1FighterMap;
    
    private Map<String, FighterEntity> camp2FighterMap;
    
    private int camp1TotalFightScore;
    
    private int camp2TotalFightScore;
    
    private Daily5v5BattleStat fightStat;
    
    private Map<String, Integer> fighterSeverIdMap;
    
    private PhasesPkFightArgs args;
    
    private long totalFight1;
    
    private long totalFight2;
    
    public BattleData() {
		// TODO Auto-generated constructor stub
	}
    
    public BattleData(MatchingTeamVo vo, MatchingTeamVo enemyVo) {
    	this.fightId = vo.getFightId();
		this.camp1Id = vo.getTeamId();
		this.camp2Id = enemyVo.getTeamId();
		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
		this.camp1FighterMap = new HashMap<String, FighterEntity>();
		int size = memberList.size();
		Daily5v5MatchingVo daily5v5MatchingVo = null;
		for(int i=0;i<size;i++){
			daily5v5MatchingVo = memberList.get(i);
			camp1FighterMap.put(String.valueOf(daily5v5MatchingVo.getRoleId()), daily5v5MatchingVo.getEntity());
		}
		List<Daily5v5MatchingVo> memberList2 = enemyVo.getMemberList();
		this.camp2FighterMap = new HashMap<String, FighterEntity>();
		int size2 = memberList2.size();
		for(int i=0;i<size2;i++){
			daily5v5MatchingVo = memberList2.get(i);
			camp2FighterMap.put(String.valueOf(daily5v5MatchingVo.getRoleId()), daily5v5MatchingVo.getEntity());
		}
		fightStat = new Daily5v5BattleStat(camp1Id, camp2Id, 100, 100);
	}
    
	public long getCamp1Id() {
		return camp1Id;
	}

	public void setCamp1Id(long camp1Id) {
		this.camp1Id = camp1Id;
	}

	public long getCamp2Id() {
		return camp2Id;
	}

	public void setCamp2Id(long camp2Id) {
		this.camp2Id = camp2Id;
	}

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

	public String getCamp1Name() {
		return camp1Name;
	}

	public void setCamp1Name(String camp1Name) {
		this.camp1Name = camp1Name;
	}

	public String getCamp2Name() {
		return camp2Name;
	}

	public void setCamp2Name(String camp2Name) {
		this.camp2Name = camp2Name;
	}

	public Map<String, FighterEntity> getCamp1FighterMap() {
		return camp1FighterMap;
	}

	public void setCamp1FighterMap(Map<String, FighterEntity> camp1FighterMap) {
		this.camp1FighterMap = camp1FighterMap;
	}

	public Map<String, FighterEntity> getCamp2FighterMap() {
		return camp2FighterMap;
	}

	public void setCamp2FighterMap(Map<String, FighterEntity> camp2FighterMap) {
		this.camp2FighterMap = camp2FighterMap;
	}

	public int getCamp1TotalFightScore() {
		return camp1TotalFightScore;
	}

	public void setCamp1TotalFightScore(int camp1TotalFightScore) {
		this.camp1TotalFightScore = camp1TotalFightScore;
	}

	public int getCamp2TotalFightScore() {
		return camp2TotalFightScore;
	}

	public void setCamp2TotalFightScore(int camp2TotalFightScore) {
		this.camp2TotalFightScore = camp2TotalFightScore;
	}

	public Daily5v5BattleStat getFightStat() {
		return fightStat;
	}

	public void setFightStat(Daily5v5BattleStat fightStat) {
		this.fightStat = fightStat;
	}

	public Map<String, Integer> getFighterSeverIdMap() {
		return fighterSeverIdMap;
	}

	public void setFighterSeverIdMap(Map<String, Integer> fighterSeverIdMap) {
		this.fighterSeverIdMap = fighterSeverIdMap;
	}

	public PhasesPkFightArgs getArgs() {
		return args;
	}

	public void setArgs(PhasesPkFightArgs args) {
		this.args = args;
	}

	public long getTotalFight1() {
		return totalFight1;
	}

	public void setTotalFight1(long totalFight1) {
		this.totalFight1 = totalFight1;
	}

	public long getTotalFight2() {
		return totalFight2;
	}

	public void setTotalFight2(long totalFight2) {
		this.totalFight2 = totalFight2;
	}

}
