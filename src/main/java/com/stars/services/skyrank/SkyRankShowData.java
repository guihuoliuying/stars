package com.stars.services.skyrank;

import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.prodata.SkyRankSeasonRankAwardVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 天梯排行显示数据
 * 
 * @author xieyuejun
 *
 */
public class SkyRankShowData implements Comparable<SkyRankShowData> {
	private long roleId;
	private String name;
	private int fightscore;
	private int serverId;
	private String serverName;
	private int score;
	private int rank;
	
	public SkyRankShowData(){}
	
	public String toString(){
		StringBuilder sbuff = new StringBuilder();
		sbuff.append(roleId).append("|")
		.append(name).append("|")
		.append(fightscore).append("|")
		.append(serverId).append("|")
		.append(serverName).append("|")
		.append(score).append("|")
		.append(rank);
		return sbuff.toString();
	}
	
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
	
	
	//select roleid,name,fightscore,score from roleskyrank where score >0 order by score desc limit 1000 ;
	public void writeBuffer(NewByteBuffer buff) {
		buff.writeInt(rank);
		buff.writeString(roleId+"");
		buff.writeString(name);
		buff.writeInt(score);
		buff.writeString(serverName);
		SkyRankSeasonRankAwardVo awardVo = SkyRankManager.getManager().getSkyRankSeasonRankAwardVo(rank);
		if(awardVo != null){
			buff.writeInt(awardVo.getDropId());
		}else{
			buff.writeInt(0);
		}
	}
	

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int compareTo(SkyRankShowData o) {
		if (o.getScore() == this.getScore()) {
			if (o.getFightscore() == this.getFightscore()) {
				if (o.getRoleId() > this.getRoleId()) {
					return 1;
				} else if (o.getRoleId() < this.getRoleId()) {
					return -1;
				}
				return 0;
			}
			return o.getFightscore() - this.getFightscore();
		}
		return o.getScore() - this.getScore();
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getFightscore() {
		return fightscore;
	}

	public void setFightscore(int fightscore) {
		this.fightscore = fightscore;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
