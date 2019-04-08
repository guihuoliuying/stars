package com.stars.multiserver.teamPVPGame;

import com.stars.core.player.PlayerPacket;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by chenkeyu on 2016/12/5.
 */
public class TPGTeam extends DbRow implements Comparable<TPGTeam>{
	private int teamId;
	private String tpgId;
	private String step;
	private int lastLose = 0;// 连续失败
	private int lastWin = 0;// 连续胜利
	private int score = 0;// 积分赛积分
	private int totalWinRing = 0;// 一个阶段累计胜利轮数(小组赛/四强赛用到)
	private int winOnceRing = 0;// 一轮的胜场(每轮过后重置)[如果不考虑一轮比赛中停服,这个也可以不存库]
    private long captainId;// 队长roleId

	/* 内存数据 */
	private Map<Long, TPGTeamMember> memberMap = new HashMap<>();
	private int fight = 0;// 队伍总战力
	private int maxMemberLevel = 0;
	private int minMemberLevel = 0;
	private String fightSceneId;// 战斗场景Id
	private int scoreRanking = 0;// 积分赛排名
	private byte[] ringStatus;// 一轮的胜利状态,0=初始/-1=失败/1=胜利,最多只能两条

	public TPGTeam(){
		ringStatus = new byte[]{0, 0};
	}

	public TPGTeam(int teamId){
		this.teamId = teamId;
	}

    @Override
    public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "tpgteam", "`tpgid`=" + tpgId + " and `teamid`=" + teamId);
	}

    @Override
    public String getDeleteSql() {
        return "";
    }

    @Override
    public int compareTo(TPGTeam o) {
    	return this.fight - o.getFight();
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(teamId);
		buff.writeString(ringStatus[0] + "," + ringStatus[1]);// 一轮的胜利状态,0=初始/-1=失败/1=胜利,最多只能两条
		buff.writeByte((byte) memberMap.size());
        for (TPGTeamMember member : memberMap.values()) {
            member.writeToBuff(buff);
        }
    }

	public void sendPacketToMember(PlayerPacket packet) {
		for (TPGTeamMember member : memberMap.values()) {
			PacketManager.send(member.getRoleId(), packet);
		}
	}

	/**
	 * 一轮比赛中的一场结束
	 * 重置fight
	 *
	 * @param isWin
	 */
	public void fieldEnd(boolean isWin) {
		setFightSceneId("");
		if (isWin) {
			setWinOnceRing(winOnceRing + 1);
		}
		byte one = ringStatus[0];
		byte two = ringStatus[1];
		if (one == 0) {
			ringStatus = new byte[]{(byte) (isWin ? 1 : -1), two};
		} else if (two == 0) {
			ringStatus = new byte[]{one, (byte) (isWin ? 1 : -1)};
		}
	}

	/**
	 * 一轮结束,
	 * 胜利轮数+1
	 * 重置胜场数
	 * 重置胜利状态
	 *
	 * @param isWin
	 */
	public void ringEnd(boolean isWin) {
		if (isWin) {
			totalWinRing = totalWinRing + 1;
		}
		winOnceRing = 0;
		// 重置胜利状态
		ringStatus = new byte[]{0, 0};
	}

	public Map<Long, Integer> getMemberDamageMap() {
		Map<Long, Integer> map = new HashMap<>();
		for (TPGTeamMember member : memberMap.values()) {
			map.put(member.getRoleId(), member.getHurt());
		}
		return map;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public void addUpdateMember(TPGTeamMember member){
		int addFightScore;
		// 在队伍中
		if (memberMap.containsKey(member.getRoleId())) {
			addFightScore = memberMap.get(member.getRoleId()).getFight() - member.getFight();
		} else {
			addFightScore = member.getFight();
		}
		this.memberMap.put(member.getRoleId(), member);
		if (addFightScore != 0) {
			fight = fight + addFightScore;
		}
	}

	public String getTpgId() {
		return tpgId;
	}

	public void setTpgId(String tpgId) {
		this.tpgId = tpgId;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public Map<Long, TPGTeamMember> getMembers() {
		return memberMap;
	}

	/**
	 * @return 战力
	 */
	public int getFight(){
		return fight;
	}

	public int getMaxMemberLevel(){
		if (maxMemberLevel > 0) {
			return maxMemberLevel;
		}
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			if (tpgTeamMember.getLevel() > maxMemberLevel) {
				maxMemberLevel = tpgTeamMember.getLevel();
			}
		}
		return maxMemberLevel;
	}


	public int getMinMemberLevel(){
		if (minMemberLevel > 0) {
			return minMemberLevel;
		}
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			if (minMemberLevel == 0 || tpgTeamMember.getLevel() < minMemberLevel) {
				minMemberLevel = tpgTeamMember.getLevel();
			}
		}
		return minMemberLevel;
	}

	public int getLastLose() {
		return lastLose;
	}

	public void setLastLose(int lastLose) {
		this.lastLose = lastLose;
	}

	public boolean allDead(){
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			if (!tpgTeamMember.isDead()) {
				return false;
			}
		}
		return true;
	}

	private int getRemainHpPercent(){
		int remain=0;
		int all = 0;
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			int maxHp = tpgTeamMember.getfEntity().getAttribute().getMaxhp();
			all = all + maxHp;
			remain = remain + (maxHp - tpgTeamMember.getHurted());
		}
		return remain*100/all;
	}

	private int getAllFight(){
		int all = 0;
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			all = all + tpgTeamMember.getFight();
		}
		return all;
	}

	public boolean isWin(TPGTeam opponent){
		if (getRemainHpPercent() > opponent.getRemainHpPercent()) {
			return true;
		}
		return getAllFight() >= opponent.getAllFight()?true:false;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int add){
		score = score + add;
	}

	/**
	 * @return 死亡次数
	 */
	public int getDeadCount(){
		int counter = 0;
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			if (tpgTeamMember.isDead()) {
				counter++;
			}
		}
		return counter;
	}

	public int getLastWin() {
		return lastWin;
	}

	public void setLastWin(int lastWin) {
		this.lastWin = lastWin;
	}

	public int getTotalWinRing() {
		return totalWinRing;
	}

	public void setTotalWinRing(int totalWinRing) {
		this.totalWinRing = totalWinRing;
	}

	public int getWinOnceRing() {
		return winOnceRing;
	}

	public void setWinOnceRing(int winOnceRing) {
		this.winOnceRing = winOnceRing;
	}

	public long getCaptainId() {
        return captainId;
    }

    public void setCaptainId(long captainId) {
        this.captainId = captainId;
    }

	public String getFightSceneId() {
		return fightSceneId;
	}

	public void setFightSceneId(String fightSceneId) {
		this.fightSceneId = fightSceneId;
	}

	public int getScoreRanking() {
		return scoreRanking;
	}

	public void setScoreRanking(int scoreRanking) {
		this.scoreRanking = scoreRanking;
	}
}
