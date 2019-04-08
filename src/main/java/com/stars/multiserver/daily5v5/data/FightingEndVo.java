package com.stars.multiserver.daily5v5.data;

public class FightingEndVo implements Comparable<FightingEndVo>{
	
	private long roleId;//玩家id
	
	private String roleName;//玩家名字
	
	private int level;//等级
	
	private int jobId;//职业
	
	private long integral;//积分
	
	private int fightValue;//战力
	
	private int killCount; // 人头
	
	private int deadCount; // 阵亡
	
    private int assistCount;//助攻
    
    private int maxComboKillCount; // 最大连斩数
	
	public FightingEndVo() {
		// TODO Auto-generated constructor stub
	}

	public FightingEndVo(long roleId, String roleName, int level, int jobId, long integral, int fightValue,
			int killCount, int deadCount, int assistCount, int maxComboKillCount) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.level = level;
		this.jobId = jobId;
		this.integral = integral;
		this.fightValue = fightValue;
		this.killCount = killCount;
		this.deadCount = deadCount;
		this.assistCount = assistCount;
		this.maxComboKillCount = maxComboKillCount;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public long getIntegral() {
		return integral;
	}

	public void setIntegral(long integral) {
		this.integral = integral;
	}

	public int getFightValue() {
		return fightValue;
	}

	public void setFightValue(int fightValue) {
		this.fightValue = fightValue;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public int getDeadCount() {
		return deadCount;
	}

	public void setDeadCount(int deadCount) {
		this.deadCount = deadCount;
	}

	public int getAssistCount() {
		return assistCount;
	}

	public void setAssistCount(int assistCount) {
		this.assistCount = assistCount;
	}

	public int getMaxComboKillCount() {
		return maxComboKillCount;
	}

	public void setMaxComboKillCount(int maxComboKillCount) {
		this.maxComboKillCount = maxComboKillCount;
	}

	@Override
	public int compareTo(FightingEndVo o) {
		if(this.integral>o.getIntegral()){
			return 1;
		}else if(this.integral==o.getIntegral()){
			if(this.fightValue>o.getFightValue()){
				return 1;
			}else if(this.fightValue==o.getFightValue()){
				return 0;
			}
		}
		return -1;
	}

}
